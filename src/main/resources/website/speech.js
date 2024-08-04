let ws = new WebSocket('ws://127.0.0.1:%SOCKET_PORT%');

window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;

/**
 * @type {SpeechRecognition}
 */
let transcriber;

let lastReset = 0;
let totalResetsBelow50ms = 0;
let isErrored = false;

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
    }

    transcriber.onend = () => {
        if (isErrored)
            return;

        console.log('Transcriber resetting...');
        ws.send(JSON.stringify({
            op: 'reset'
        }));
        transcriber.start();

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
                return;
            }

            setupTranscriber(lang);

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