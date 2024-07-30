let ws = new WebSocket('ws://0.0.0.0:%SOCKET_PORT%');

/**
 * @type {SpeechRecognition}
 */
let transcriber;

ws.onopen = () => {
    console.log('Connected');
}

function setupTranscriber(lang) {
    transcriber = new SpeechRecognition();

    transcriber.lang = lang;
    transcriber.continuous = true;
    transcriber.interimResults = true;
    transcriber.maxAlternatives = 3;

    transcriber.onerror = (ev) => {
        console.error(ev.error);
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
    }

    transcriber.start();
}

ws.onmessage = (msg) => {
    const data = JSON.parse(msg);

    switch (data.op) {
        case 'set_language': {
            const lang = data.d.language;

            if (!!transcriber) {
                transcriber.stop();
            }

            setupTranscriber(lang);

            break;
        }
    }
}

ws.onclose = () => {
    if (!!transcriber) {
        transcriber.stop();
    }

    setTimeout(() => {
        ws = new WebSocket('ws://0.0.0.0:%SOCKET_PORT%');
    }, 15_000);
}