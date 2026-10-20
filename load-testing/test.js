import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';

const READ_RATE = 1200;
const WRITE_RATE = 12;
const DURATION = '30s';

export const options = {
    scenarios: {
        reads: {
            executor: 'constant-arrival-rate',

            rate: READ_RATE,
            timeUnit: '1s',
            duration: DURATION,

            // Give k6 enough concurrency to sustain the arrival rate.
            preAllocatedVUs: 400,
            maxVUs: 1500,

            exec: 'readTest',
        },

        writes: {
            executor: 'constant-arrival-rate',

            rate: WRITE_RATE,
            timeUnit: '1s',
            duration: DURATION,

            preAllocatedVUs: 20,
            maxVUs: 100,

            exec: 'writeTest',
        },
    },
};

function createPaste() {
    const payload = JSON.stringify({
        content: 'CuraPaste load test',
        expiresInSeconds: 3600,
        burnAfterRead: false,
    });

    return http.post(
        `${BASE_URL}/api/v1/pastes`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );
}

export function setup() {
    const pasteIds = [];

    // Warm up Redis with real pastes before the benchmark.
    for (let i = 0; i < 100; i++) {
        const response = createPaste();

        if (response.status !== 201) {
            throw new Error(
                `Failed to create test paste: ${response.status}`
            );
        }

        const body = JSON.parse(response.body);
        pasteIds.push(body.shortId);
    }

    console.log(`Created ${pasteIds.length} test pastes`);

    return { pasteIds };
}

export function writeTest() {
    const response = createPaste();

    check(response, {
        'POST returned 201': (r) => r.status === 201,
    });
}

export function readTest(data) {
    const index = Math.floor(
        Math.random() * data.pasteIds.length
    );

    const shortId = data.pasteIds[index];

    const response = http.get(
        `${BASE_URL}/api/v1/pastes/${shortId}`
    );

    check(response, {
        'GET returned 200': (r) => r.status === 200,
    });
}

//Test-Results

/**
 * 
 * docker compose run --rm k6 run /scripts/test.js
Container curapaste-load-testing-k6-run-5b39523fcd97 Creating 
Container curapaste-load-testing-k6-run-5b39523fcd97 Created 

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 


     execution: local
        script: /scripts/test.js
        output: -

     scenarios: (100.00%) 2 scenarios, 1600 max VUs, 1m0s max duration (incl. graceful stop):
              * reads: 1200.00 iterations/s for 30s (maxVUs: 400-1500, exec: readTest, gracefulStop: 30s)
              * writes: 12.00 iterations/s for 30s (maxVUs: 20-100, exec: writeTest, gracefulStop: 30s)

INFO[0015] Created 100 test pastes                       source=console


  █ TOTAL RESULTS 

    checks_total.......: 36363   798.268694/s
    checks_succeeded...: 100.00% 36363 out of 36363
    checks_failed......: 0.00%   0 out of 36363

    ✓ GET returned 200
    ✓ POST returned 201

    HTTP
    http_req_duration..............: avg=34.4ms min=2.06ms med=14.39ms max=924.51ms p(90)=98.91ms  p(95)=142.27ms
      { expected_response:true }...: avg=34.4ms min=2.06ms med=14.39ms max=924.51ms p(90)=98.91ms  p(95)=142.27ms
    http_req_failed................: 0.00%  0 out of 36463
    http_reqs......................: 36463  800.463972/s

    EXECUTION
    iteration_duration.............: avg=35.6ms min=2.16ms med=14.46ms max=1.13s    p(90)=100.37ms p(95)=143.61ms
    iterations.....................: 36363  798.268694/s
    vus............................: 8      min=0          max=293
    vus_max........................: 420    min=420        max=420

    NETWORK
    data_received..................: 8.1 MB 179 kB/s
    data_sent......................: 3.8 MB 83 kB/s




running (0m45.6s), 0000/0420 VUs, 36363 complete and 0 interrupted iterations
reads  ✓ [======================================] 0000/0400 VUs  30s  1200.00 iters/s
writes ✓ [======================================] 000/020 VUs    30s  12.00 iters/s
PS D:\SystemDesign\production-projects\CuraPaste\load-testing> 
 */