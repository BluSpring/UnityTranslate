let ws = new WebSocket('ws://127.0.0.1:%SOCKET_PORT%');

window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;
window.SpeechGrammarList = window.webkitSpeechGrammarList || window.SpeechGrammarList;  

const pause = document.getElementById('pause');

/**
 * @type {SpeechRecognition}
 */
let transcriber;

let lastReset = 0;
let totalResetsBelow50ms = 0;
let isErrored = false;
let wasNoSpeech = false;
let isCurrentlyMuted = false;

let grammars = [];

ws.onopen = () => {
    console.log('Connected');
}

function setupTranscriber(lang) {
    console.log(`Starting transcriber with lang ${lang}`);
    transcriber = new SpeechRecognition();

    lastReset = Date.now();

    ws.send(JSON.stringify({
        op: 'reset'
    }));

    transcriber.lang = lang;
    transcriber.continuous = true;
    transcriber.interimResults = true;
    transcriber.maxAlternatives = 3;

    transcriber.onerror = (ev) => {
        console.error(ev.error);
        if (ev.error == 'no-speech')
            wasNoSpeech = true;
    }

    transcriber.onend = () => {
        if (isErrored) {
            pause.classList.add('visible');
            return;
        }

        if (isCurrentlyMuted) {
            return;
        }

        console.log('Transcriber resetting...');
        ws.send(JSON.stringify({
            op: 'reset'
        }));
        transcriber.start();

        if (wasNoSpeech) {
            wasNoSpeech = false;
            return;
        }

        if (Date.now() - lastReset >= 50) {
            if (++totalResetsBelow50ms >= 30) {
                ws.send(JSON.stringify({
                    op: "error",
                    d: {
                        type: "too_many_resets"
                    }
                }));
                isErrored = true;
            }
        } else {
            totalResetsBelow50ms = 0;
        }
        lastReset = Date.now();
    }

    transcriber.onresult = (ev) => {
        let results = [];
        const items = ev.results.item(ev.resultIndex);
            
        for (let j = 0; j < items.length; j++) {
            const data = items.item(j);
            results.push({
                text: data.transcript,
                confidence: data.confidence
            });
        }

        ws.send(JSON.stringify({
            op: 'transcript',
            d: {
                results: results,
                index: ev.resultIndex
            }
        }));

        if (results.length > 0)
            document.getElementById('transcript').innerText = results[0].text;
    }

    transcriber.start();
}

ws.onmessage = (ev) => {
    const data = JSON.parse(ev.data);

    switch (data.op) {
        case 'set_language': {
            const lang = data.d.language;

            if (!!transcriber) {
                transcriber.stop();
            }

            if (!SpeechRecognition) {
                ws.send(JSON.stringify({
                    op: "error",
                    d: {
                        type: "no_support"
                    }
                }));
                pause.classList.add('visible');
                return;
            }

            document.getElementById('transcript_lang').innerText = `%I18N_TRANSCRIPT% (${lang})`;

            setupTranscriber(lang);

            break;
        }

        case 'set_muted': {
            const isMuted = data.d.muted;
            isCurrentlyMuted = isMuted;

            if (!isMuted && !!transcriber) {
                ws.send(JSON.stringify({
                    op: 'reset'
                }));
                transcriber.start();
            } else if (!!transcriber) {
                transcriber.stop();
            }
        }

        case 'set_grammars': {
            const grammars = new SpeechGrammarList();

            for (const grammar of data.d.grammars) {
                grammars.addFromString(grammar, 1);
            }

            transcriber.grammars = grammars;

            break;
        }
    }
}

ws.onerror = e => {
    console.error('Failed to connect!');
    console.error(e);
}

ws.onclose = () => {
    if (!!transcriber) {
        isErrored = true;
        transcriber.stop();
    }
}