Concurrent write-heavy and mixed workload experiment
Created: 2026-10-29 19:51:47
Workloads: cart-update and cart-mixed
Protocols: REST, GraphQL, gRPC
Virtual users: 1, 5, 10, 20
Repetitions per combination: 5
Duration per run: 60 seconds
Dedicated warm-up phase: none
Shared target: cart ID 1, item ID 4
Before each run: reset item quantity to 2 and verify it outside the timed test
Timing metric: group_duration
All run logs and JSON summaries are retained in this folder.
