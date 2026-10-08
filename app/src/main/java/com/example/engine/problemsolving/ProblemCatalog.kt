package com.example.engine.problemsolving

object ProblemCatalog {

    val challenges: List<ProblemChallenge> = listOf(
        // ==========================================
        // 1. CODING CHALLENGE: Lock-Free Ring Buffer (MPMC)
        // ==========================================
        ProblemChallenge(
            id = "code-lockfree-ring-buffer",
            title = "Lock-Free MPMC Ring Buffer",
            domain = ProblemDomain.CODING,
            difficulty = ProblemDifficulty.HARD,
            subtitle = "Design a multi-producer multi-consumer queue with zero locks and cache-line padding",
            description = """
Design a high-throughput, bounded Multi-Producer Multi-Consumer (MPMC) Ring Buffer without using mutexes or synchronized blocks.

### Requirements:
1. Multiple threads concurrently enqueue and dequeue elements without blocking other threads.
2. Must handle buffer wrap-around safely using a sequence-per-cell protocol or atomic index masking.
3. Must avoid the ABA problem and false sharing across CPU L1 cache lines (64-byte alignment).
4. Provide lock-free offer(element) and poll() methods returning true/false immediately if full/empty.
            """.trimIndent(),
            constraints = listOf(
                "Buffer capacity must be a power of 2 (e.g. 1024, 4096) for fast bitwise masking: index & (capacity - 1).",
                "No synchronized, ReentrantLock, or OS-level sleep calls allowed.",
                "Memory orderings must adhere to Java volatile memory model (Acquire/Release semantics).",
                "Linearizability: any successful poll() must return the value committed by the corresponding offer()."
            ),
            starterPremiseOrCode = """
class LockFreeRingBuffer<T>(val capacity: Int) {
    init {
        require(capacity > 0 && (capacity and (capacity - 1)) == 0) { "Capacity must be power of 2" }
    }
    
    class Cell<T>(
        @Volatile var sequence: Long = 0L,
        var element: T? = null
    )
    
    // TODO: Define head, tail, and buffer cells array
    fun offer(item: T): Boolean {
        // Implement atomic enqueue
        return false
    }
    
    fun poll(): T? {
        // Implement atomic dequeue
        return null
    }
}
            """.trimIndent(),
            hints = listOf(
                "Hint 1: Maintain a monotonic sequence for every cell. A cell at index i is writable when its sequence == current_tail.",
                "Hint 2: When an enqueuer commits, it updates cell sequence to current_tail + 1. This signals to consumers that the cell data is visible.",
                "Hint 3: Use AtomicLong for head and tail monotonic counters. An enqueuer first CASes tail from t to t + 1. If cell.sequence == t, write data, then publish by setting cell.sequence = t + 1."
            ),
            approach1Summary = "Coarse-Grained ReentrantLock with Condition variables. Easy to write, but suffers heavy thread contention and context-switching overhead (~1,200ns per op).",
            approach2Summary = "Dmitry Vyukov's MPMC bounded queue with sequence tracking array. Lock-free CAS with cache padding (~14ns per op, 80x faster).",
            optimalSolutionSnippet = """
class LockFreeMPMCQueue<T>(val capacity: Int) {
    private val mask = capacity - 1
    private val buffer = Array(capacity) { index -> Cell<T>(sequence = index.toLong()) }
    private val tail = java.util.concurrent.atomic.AtomicLong(0L)
    private val head = java.util.concurrent.atomic.AtomicLong(0L)

    class Cell<T>(@Volatile var sequence: Long, @Volatile var item: T? = null)

    fun offer(value: T): Boolean {
        var currentTail = tail.get()
        while (true) {
            val cell = buffer[(currentTail and mask.toLong()).toInt()]
            val seq = cell.sequence
            val dif = seq - currentTail
            if (dif == 0L) {
                if (tail.compareAndSet(currentTail, currentTail + 1)) {
                    cell.item = value
                    cell.sequence = currentTail + 1
                    return true
                }
            } else if (dif < 0L) {
                return false // Queue is full
            } else {
                currentTail = tail.get()
            }
        }
    }

    fun poll(): T? {
        var currentHead = head.get()
        while (true) {
            val cell = buffer[(currentHead and mask.toLong()).toInt()]
            val seq = cell.sequence
            val dif = seq - (currentHead + 1)
            if (dif == 0L) {
                if (head.compareAndSet(currentHead, currentHead + 1)) {
                    val result = cell.item
                    cell.item = null
                    cell.sequence = currentHead + mask + 1
                    return result
                }
            } else if (dif < 0L) {
                return null // Queue is empty
            } else {
                currentHead = head.get()
            }
        }
    }
}
            """.trimIndent(),
            formalProofOrExplanation = """
### Deep Thinking Invariant Proof:
1. Single Owner Invariant: At any tick t, a cell can only be claimed by exactly one producer because tail.compareAndSet is executed before writing, and each cell sequence seq == currentTail holds only for the producer owning slot currentTail.
2. Memory Visibility: Producers write cell.item = value BEFORE updating cell.sequence = currentTail + 1. Because sequence is marked @Volatile, the write establishes a happens-before relationship with the consumer reading cell.sequence == currentHead + 1.
3. Zero False Sharing: To achieve peak 85M ops/sec, individual cells or index variables should be padded with 56 dummy bytes to prevent cache bounce across CPU cores.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Verified buffer wrap-around over 100,000,000 transactions without sequence overflow issues.",
                "Verified thread safety under 16 concurrent producers and 16 concurrent consumers.",
                "Confirmed zero memory leaks and proper nullification of polled items for garbage collection.",
                "Tested buffer full and empty edge cases returning false/null without deadlocks."
            ),
            tags = listOf("Concurrency", "Lock-Free", "MPMC", "Atomics", "Data Structures")
        ),

        // ==========================================
        // 2. CODING CHALLENGE: TSP Bitmask Dynamic Programming
        // ==========================================
        ProblemChallenge(
            id = "code-tsp-bitmask-dp",
            title = "Hamiltonian Path & Bitmask DP",
            domain = ProblemDomain.CODING,
            difficulty = ProblemDifficulty.HARD,
            subtitle = "Compute minimum-cost topological tour visiting all nodes exactly once using state compression",
            description = """
Given a complete weighted directed graph with N vertices (1 <= N <= 20), find the minimum total weight Hamiltonian cycle that starts at vertex 0, visits all other vertices exactly once, and returns to vertex 0.

Standard recursive permutation requires O(N!) operations, which is completely infeasible for N=20 (20! is approx 2.43 * 10^18). Design an exact polynomial-exponential Dynamic Programming solution utilizing bitmask state compression.
            """.trimIndent(),
            constraints = listOf(
                "Number of vertices N <= 20.",
                "Weights W[u][v] >= 0 for all pairs u, v.",
                "Time complexity must not exceed O(2^N * N^2).",
                "Space complexity must be bounded by O(2^N * N)."
            ),
            starterPremiseOrCode = """
fun tspBitmask(n: Int, dist: Array<IntArray>): Int {
    val INF = 1_000_000_000
    val totalMasks = 1 shl n
    val dp = Array(totalMasks) { IntArray(n) { INF } }
    
    // Base case: at start node 0 with only node 0 visited
    dp[1][0] = 0
    
    // TODO: Iterate over masks and transitions
    return 0
}
            """.trimIndent(),
            hints = listOf(
                "Hint 1: Represent the set of visited cities as an integer bitmask. If bit k is 1, city k has been visited.",
                "Hint 2: Define dp[mask][u] as the minimum cost to visit all cities in mask, ending at city u.",
                "Hint 3: State transition: for any unvisited city v where (mask and (1 shl v)) == 0, dp[mask or (1 shl v)][v] = min(dp[mask or (1 shl v)][v], dp[mask][u] + dist[u][v])."
            ),
            approach1Summary = "Brute Force DFS Permutations: Evaluates all N! trajectories. Exhausts memory and CPU at N >= 13.",
            approach2Summary = "Held-Karp Bitmask DP: Compresses visited subset into bits 0 to (2^N - 1). Total operations for N=20 is 2^20 * 400 approx 4.19 * 10^8 operations, resolving in < 450 ms.",
            optimalSolutionSnippet = """
fun tspHeldKarp(n: Int, dist: Array<IntArray>): Int {
    val INF = 1_000_000_000
    val totalStates = 1 shl n
    val dp = Array(totalStates) { IntArray(n) { INF } }

    dp[1][0] = 0 // Start at city 0 with mask 000...001

    for (mask in 1 until totalStates) {
        for (u in 0 until n) {
            if ((mask and (1 shl u)) == 0) continue
            val currentCost = dp[mask][u]
            if (currentCost >= INF) continue

            for (v in 0 until n) {
                if ((mask and (1 shl v)) == 0) {
                    val nextMask = mask or (1 shl v)
                    val newCost = currentCost + dist[u][v]
                    if (newCost < dp[nextMask][v]) {
                        dp[nextMask][v] = newCost
                    }
                }
            }
        }
    }

    // Return to starting node 0 from all full-tour endpoints
    val fullMask = (1 shl n) - 1
    var minTour = INF
    for (lastNode in 1 until n) {
        if (dp[fullMask][lastNode] < INF) {
            val totalCost = dp[fullMask][lastNode] + dist[lastNode][0]
            if (totalCost < minTour) {
                minTour = totalCost
            }
        }
    }
    return minTour
}
            """.trimIndent(),
            formalProofOrExplanation = """
### Principle of Optimality (Bellman Equation):
Sub-tours possess optimal substructure. If the optimal Hamiltonian cycle traverses subset S ending at u, the prefix path traversing S without u must itself be the minimum-weight path from 0 to some w in S without u.
By ordering the evaluation by increasing mask value (numeric integer order naturally respects topological dependency since (mask or (1 shl v)) > mask), no cyclic memoization dependencies occur.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Hand-tested for triangle inequality graph N=4 matching manual permutation.",
                "Handled disconnected or high-cost edges with INF boundary checks.",
                "Memory consumption for N=20: 2^20 * 20 * 4 bytes approx 83.8 MB, perfectly fits inside mobile RAM."
            ),
            tags = listOf("Dynamic Programming", "Bitmask", "Graphs", "NP-Hard", "Held-Karp")
        ),

        // ==========================================
        // 3. LOGICAL PUZZLE: The Three Gods Riddle (Boolos)
        // ==========================================
        ProblemChallenge(
            id = "logic-three-gods-paradox",
            title = "The Three Gods Paradox (Boolos)",
            domain = ProblemDomain.LOGIC,
            difficulty = ProblemDifficulty.OLYMPIAD,
            subtitle = "Deduce the identities of Truth, False, and Random using only 3 Yes/No questions in unknown language",
            description = """
Three gods A, B, and C are called, in no particular order, Truth, False, and Random.
- Truth always speaks truly.
- False always speaks falsely.
- Random speaks truly or falsely at random (as by an internal coin flip).

You must determine the identities of A, B, and C by asking three yes-no questions; each question must be put to exactly one god.

### Crucial Complication:
The gods understand English, but will answer in their own language, in which the words for Yes and No are 'da' and 'ja', in some unknown order. You do not know which word means Yes and which means No!
            """.trimIndent(),
            constraints = listOf(
                "Exactly 3 questions total across all gods.",
                "Each question must be a binary question answered with 'da' or 'ja'.",
                "Random answers completely unpredictably (50/50 probability), conveying zero information.",
                "You must uniquely identify who is Truth, who is False, and who is Random."
            ),
            starterPremiseOrCode = """
// Formulate Question 1 to God A:
// Goal: Identify a god (either B or C) who is GUARANTEED NOT TO BE RANDOM.
// Premise: Compound counterfactual question embedding both the truth-value and the word definition.
            """.trimIndent(),
            hints = listOf(
                "Hint 1: If you ask Random, their answer provides zero bits of entropy. Therefore, Question 1 MUST be used to locate a god who is NOT Random (either B or C).",
                "Hint 2: How to bypass the unknown meaning of 'da' and 'ja'? Use an embedded conditional: 'Does da mean Yes if and only if Q is true?'",
                "Hint 3: Construct the question: 'If I asked you Q, would you say ja?' If the god is Truth, they say 'ja' iff Q is true. If the god is False, they ALSO say 'ja' iff Q is true! The double lie cancels out!"
            ),
            approach1Summary = "Naive Direct Questions: 'Are you Truth?' - If answered 'da', it could mean Yes or No, and Random might answer anything. Leaves 6 ambiguous permutations.",
            approach2Summary = "Lemma of the Double Negation / Counterfactual Gate: Eliminates Random with Question 1, then queries the discovered deterministic god (Truth or False) with Questions 2 and 3.",
            optimalSolutionSnippet = """
### Formal 3-Question Protocol:

Define Operator: ASK(God, Statement Q):
Ask: "If I were to ask you Q, would you say 'ja'?"
Theorem: Any deterministic god (Truth or False) answers 'ja' if and only if Q is true!

1. Question 1 to God A:
   Q: "Is B Random?"
   - If A answers 'ja', then either:
     a) A is Random.
     b) A is Truth/False, so B is Random.
     In either case, C IS NOT RANDOM! (C is deterministic).
   - If A answers 'da', then B IS NOT RANDOM!

2. Question 2 to the Confirmed Deterministic God (let's say B):
   Q: "Are you Truth?"
   - If 'ja', then B is Truth.
   - If 'da', then B is False.

3. Question 3 to the Same Deterministic God B:
   Q: "Is A Random?"
   - If 'ja', A is Random (leaving C as False/Truth).
   - If 'da', C is Random (leaving A as False/Truth).
            """.trimIndent(),
            formalProofOrExplanation = """
### Truth Table Verification for Counterfactual Question:
Let Q be the hypothesis. Let TruthVal(Q) be True or False.
Let Word Meaning: Case 1: 'ja' = Yes, 'da' = No. Case 2: 'ja' = No, 'da' = Yes.

- Suppose God is Truth:
  - If Q is True: If asked Q, Truth would say 'Yes' (which is 'ja' in Case 1). So when asked 'Would you say ja?', Truth says 'Yes' -> answers 'ja'.
  - If Case 2: Truth would say 'Yes' (which is 'da'). Would Truth say 'ja'? No! Truth says 'No' (which is 'ja' in Case 2). Truth answers 'ja'!
- Suppose God is False:
  - If Q is True: If asked Q, False would say 'No'. When asked 'Would you say ja?', False must lie about their prospective answer! The two falsifications cancel: False ALSO answers 'ja'!

Conclusion: Regardless of whether 'ja' means Yes or No, and regardless of whether the deterministic god is Truth or False, they answer 'ja' if and only if Q is True.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Elimination of Random in Q1 holds for all 6 permutations (T,F,R), (T,R,F), (F,T,R), (F,R,T), (R,T,F), (R,F,T).",
                "Double cancellation of False proven mathematically isomorphic to XNOR logic gate.",
                "Total question count exactly 3."
            ),
            tags = listOf("Logic", "Riddles", "Boolean Algebra", "Boolos", "Information Theory")
        ),

        // ==========================================
        // 4. LOGICAL PUZZLE: 100 Explorers & The Permutation Cycle
        // ==========================================
        ProblemChallenge(
            id = "logic-100-explorers-problem",
            title = "100 Explorers & Permutation Cycles",
            domain = ProblemDomain.LOGIC,
            difficulty = ProblemDifficulty.HARD,
            subtitle = "Transform an independent 1/2^100 probability into over 31.18% using cycle theory",
            description = """
A team of 100 explorers numbered 1 to 100 faces a cooperative mystery puzzle.

A room contains a cabinet with 100 numbered drawers. The game master randomly places a card with an explorer's number into each closed drawer (a random bijection).
- Each explorer enters the room one by one and may open up to 50 drawers to find their own number.
- The drawers are closed again before the next explorer enters.
- If every single explorer finds their own number in their 50 picks, the whole team wins.
- If even one explorer fails to locate their number, the team loses the game.

Explorers may coordinate a strategy beforehand, but no communication or physical markings are permitted once the challenge begins.

If each explorer opens 50 drawers independently and randomly, the group win probability is virtually zero: (1/2)^100 approx 8 * 10^-31.

Devise a cooperative strategy that raises the group victory probability to over 31.18%!
            """.trimIndent(),
            constraints = listOf(
                "No physical marks on drawers or signaling between team members.",
                "Every explorer must locate their own card within at most 50 drawer openings.",
                "Cards are arranged in a uniform random permutation sigma in S_100."
            ),
            starterPremiseOrCode = """
// Formulate the permutation cycle-following algorithm:
// Explorer k enters the room:
// 1. Open drawer numbered k.
// 2. Inspect card number inside: x_1.
// 3. If x_1 == k, success!
// 4. Else, open drawer numbered x_1, inspect card x_2...
// 5. Repeat until found or 50 drawers opened.
            """.trimIndent(),
            hints = listOf(
                "Hint 1: If choices are independent, multiplying probabilities results in near zero. The team needs their individual successes to be strongly correlated!",
                "Hint 2: The drawers and their card contents define a permutation sigma on the set {1, ..., 100}. Every permutation decomposes into disjoint cycles.",
                "Hint 3: What happens if each explorer follows the cycle starting from their own ID? If a cycle has length <= 50, everyone belonging to that cycle succeeds!"
            ),
            approach1Summary = "Independent Random Selection: P = (1/2)^100 approx 7.89 * 10^-31. Essentially zero chance of success.",
            approach2Summary = "Cycle Following Strategy: All explorers succeed simultaneously if and only if the permutation contains NO cycle of length L > 50. Probability = 1 - sum_{L=51}^{100} 1/L = 1 - (H_100 - H_50) approx 1 - ln(2) approx 31.1837%.",
            optimalSolutionSnippet = """
fun simulateExplorerCycleStrategy(trials: Int = 100_000): Double {
    var victories = 0
    val n = 100
    val maxOpenings = 50

    for (trial in 0 until trials) {
        val drawers = (1..n).shuffled() // 1-indexed random permutation
        var maxCycleLength = 0
        val visited = BooleanArray(n + 1)

        for (i in 1..n) {
            if (!visited[i]) {
                var current = i
                var length = 0
                while (!visited[current]) {
                    visited[current] = true
                    current = drawers[current - 1]
                    length++
                }
                if (length > maxCycleLength) maxCycleLength = length
            }
        }

        // All succeed if no cycle exceeds 50
        if (maxCycleLength <= maxOpenings) {
            victories++
        }
    }
    return victories.toDouble() / trials // Yields ~0.3118
}
            """.trimIndent(),
            formalProofOrExplanation = """
### Combinatoric Cycle Decomposition Proof:
In the symmetric group S_n, how many permutations have a cycle of length L > n/2?
Because L > n/2, there can be at most ONE such cycle.

1. Choose L elements out of n: n! / (L! * (n - L)!).
2. Number of distinct cycles of length L: (L - 1)!.
3. Number of permutations of remaining (n - L) elements: (n - L)!.

Total permutations with a cycle of length L:
(n! / (L! * (n - L)!)) * (L - 1)! * (n - L)! = n! / L.

Dividing by n! permutations, the probability is:
P(cycle of length L) = 1 / L.

Since cycles of length L > 50 are mutually exclusive:
P(failure) = sum_{L=51}^{100} (1 / L) = H_100 - H_50 approx ln(2) approx 0.6931.

Therefore:
P(success) = 1 - sum_{L=51}^{100} (1 / L) approx 1 - ln(2) = 0.3118 (31.18%).
            """.trimIndent(),
            verificationChecklist = listOf(
                "Verified harmonic sum formula 1 - (H_100 - H_50) = 0.31182782...",
                "Correlated probability eliminates independence trap.",
                "Algorithm works identically for n = 10^6 with asymptotic limit 1 - ln(2)."
            ),
            tags = listOf("Probability", "Combinatorics", "Permutation Cycles", "Information Theory")
        ),

        // ==========================================
        // 5. SCIENTIFIC QUESTION: Quantum Decoherence & Surface Codes
        // ==========================================
        ProblemChallenge(
            id = "science-quantum-decoherence",
            title = "Quantum Decoherence & Surface Code Thresholds",
            domain = ProblemDomain.SCIENCE,
            difficulty = ProblemDifficulty.HARD,
            subtitle = "Analyze environmental entangling kinetics and topological error correction syndrome extraction",
            description = """
A superconducting transmon qubit initialized in a coherent superposition |psi> = (1/sqrt(2))(|0> + |1>) experiences environmental phase dampening (pure dephasing) and energy relaxation (T_1, T_2).

### Investigation Scope:
1. Derive the density matrix evolution rho(t) under the Lindblad master equation for pure dephasing with dephasing rate gamma_phi.
2. Explain how surface code quantum error correction (the rotated planar code) detects both bit-flip (X) and phase-flip (Z) Pauli errors using local stabilizer weight-4 measurements without collapsing the superposition.
3. What is the physical significance of the surface code error threshold (~1%), and why does local syndrome measurement evade the No-Cloning Theorem?
            """.trimIndent(),
            constraints = listOf(
                "Maintain density matrix trace preservation: Tr(rho) = 1.",
                "Stabilizer operators must commute [A_p, B_q] = 0 for all plaquettes p, q.",
                "Explain the distinction between T_1 (relaxation) and T_2* (dephasing) timescales: 1/T_2 = 1/(2*T_1) + 1/T_phi."
            ),
            starterPremiseOrCode = """
// Density Matrix for pure state:
// rho(0) = |psi><psi| = [[0.5, 0.5], [0.5, 0.5]]
// Lindblad Equation: d(rho)/dt = -i[H, rho] + gamma * (Z rho Z - rho)
            """.trimIndent(),
            hints = listOf(
                "Hint 1: In the presence of pure dephasing (jump operator L = sqrt(gamma_phi) * Z), diagonal elements (populations) remain constant, while off-diagonal coherences decay as e^(-gamma_phi * t).",
                "Hint 2: Syndrome measurements measure parity across 4 data qubits. Measuring Z1*Z2*Z3*Z4 reveals whether an odd or even number of X errors occurred, without revealing whether the state was |0...0> or |1...1>.",
                "Hint 3: The No-Cloning Theorem prevents duplicating an unknown quantum state, but error correction does not clone: it projects entropy onto ancilla qubits which are then measured and reset to heat sinks."
            ),
            approach1Summary = "Classical Redundancy (Triplication Code): Fails for quantum states because measuring copies collapses state and bit-flip codes do not correct phase flips.",
            approach2Summary = "Topological Surface Code: 2D lattice of alternating data and ancilla qubits. Employs Minimum Weight Perfect Matching (MWPM) to decode error chains across non-trivial homological cycles.",
            optimalSolutionSnippet = """
### Decoherence Evolution:
rho(t) = [[0.5, 0.5 * exp(-t/T_2)], [0.5 * exp(-t/T_2), 0.5]]
Where 1/T_2 = 1/(2*T_1) + 1/T_phi.

### Plaquette Stabilizers:
- Star Operator: A_s = X1 * X2 * X3 * X4 (Detects Phase errors Z)
- Plaquette Operator: B_p = Z1 * Z2 * Z3 * Z4 (Detects Bit errors X)

Because each star and plaquette share either 0 or 2 edges, their operators commute:
X1*X2 * Z1*Z2 = (-1)^2 * Z1*Z2 * X1*X2 = Z1*Z2 * X1*X2
Thus, all syndromes can be measured simultaneously without cross-talk disturbance!
            """.trimIndent(),
            formalProofOrExplanation = """
### Deep Physical Synthesis:
1. Lindbladian Off-Diagonal Decay:
   d(rho_01)/dt = -(1/(2*T_1) + gamma_phi) * rho_01 => rho_01(t) = rho_01(0) * exp(-t/T_2)
   When t >> T_2, rho(t) -> diag(0.5, 0.5), representing a classical statistical mixture with zero quantum interference capability.
2. Evading No-Cloning via Homology:
   The logical qubit is stored non-locally in the global topology of the lattice. Local physical environmental noise creates isolated point-like syndrome pairs (anyons). As long as the error rate p < p_th approx 1%, the MWPM decoder pairs anyons correctly before an error chain spans across the entire lattice.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Commutator algebra [A_s, B_p] = 0 holds for 2-qubit intersection.",
                "Purity Tr(rho^2) = 0.5*(1 + exp(-2t/T_2)) verified: decays from 1.0 (pure) to 0.5 (maximally mixed).",
                "Fault-tolerant threshold p_th approx 1.05% matches Toric/Surface code 3D Ising model mapping."
            ),
            tags = listOf("Quantum Physics", "Decoherence", "Surface Codes", "Qubits", "Thermodynamics")
        ),

        // ==========================================
        // 6. SCIENTIFIC QUESTION: Maxwell's Demon & Landauer's Principle
        // ==========================================
        ProblemChallenge(
            id = "science-maxwells-demon-landauer",
            title = "Maxwell's Demon & Landauer's Principle",
            domain = ProblemDomain.SCIENCE,
            difficulty = ProblemDifficulty.HARD,
            subtitle = "Exorcise the thermodynamic paradox by demonstrating how information processing consumes entropy",
            description = """
In 1867, James Clerk Maxwell proposed a thought experiment: a microscopic creature ('Demon') controls a frictionless trapdoor between two gas chambers A and B. By observing incoming particles, the Demon opens the door to allow fast molecules into A and slow molecules into B.

This creates a temperature differential without performing mechanical work, seemingly decreasing the thermodynamic entropy (Delta S < 0) in direct violation of the Second Law of Thermodynamics.

### Challenge:
1. Resolve the paradox using modern statistical mechanics and information theory (Szilard Engine and Landauer's Principle).
2. Prove where entropy is generated in the physical cycle: is it during particle measurement, or during memory erasure?
3. Derive Landauer's lower bound for heat dissipation: Q = k_B * T * ln(2) per erased bit.
            """.trimIndent(),
            constraints = listOf(
                "Gas behaves as an ideal gas: PV = N * k_B * T.",
                "Total entropy of Universe Delta S_gas + Delta S_demon + Delta S_reservoir >= 0.",
                "Explain Bennett's reversible measurement resolution (1982)."
            ),
            starterPremiseOrCode = """
// Szilard 1-Molecule Engine:
// 1 molecule in chamber of volume V.
// Partition inserted in middle -> V/2 left, V/2 right.
// Demon measures molecule position: 1 bit of information (L or R).
// Weight attached to piston -> isothermal expansion extracts W = k_B * T * ln(2) work!
            """.trimIndent(),
            hints = listOf(
                "Hint 1: Szilard initially thought measurement generated the entropy. However, Charles Bennett proved that measurement can theoretically be performed reversibly with zero dissipation!",
                "Hint 2: The Demon possesses finite physical memory. To complete the thermodynamic cycle and return to its initial state, the Demon MUST ERASE the stored bit!",
                "Hint 3: Erasure is a many-to-one logical operation ({0, 1} -> 0). Compressing the phase space of the Demon's memory reduces its entropy by Delta S = k_B * ln(2), which must be ejected as heat into the environment: Q = T * Delta S."
            ),
            approach1Summary = "Measurement Cost Hypothesis (Szilard/Brillouin): Believed photon scattering during observation dissipates >= k_B * T * ln(2). (Refuted by Bennett).",
            approach2Summary = "Landauer-Bennett Information Thermodynamics: Measurement is thermodynamically reversible; the mandatory entropic cost occurs during memory reset/erasure to close the cycle.",
            optimalSolutionSnippet = """
### Thermodynamic Ledger of 1-Molecule Szilard Cycle:

1. Insertion of Partition: Delta S_1 = 0, W_1 = 0.
2. Measurement: Demon stores 'L' or 'R' in memory. Delta S_meas = 0 (Reversible).
3. Isothermal Expansion: Work extracted from single thermal bath:
   W = integral_{V/2}^{V} P dV = k_B * T * ln(V / (V/2)) = k_B * T * ln(2).
   Entropy of reservoir: Delta S_bath = -Q / T = -k_B * ln(2).
4. Memory Erasure (Closing Cycle):
   Reset memory state {0, 1} -> 0.
   Reduction of Demon entropy: Delta S_memory = -k_B * ln(2).
   To satisfy 2nd Law, heat ejected to environment: Q_dissipated >= k_B * T * ln(2).
   Net Universe Entropy: Delta S_total = (-k_B * ln(2)) + (+k_B * ln(2)) = 0 >= 0.
            """.trimIndent(),
            formalProofOrExplanation = """
### Landauer's Principle Derivation:
Consider a physical bit implemented as a symmetric bistable potential well with state 0 in the left well and state 1 in the right well, immersed in a heat bath at temperature T.

The phase space volume of the un-erased bit corresponds to 2 macrostates:
Omega_initial = 2 => S_initial = k_B * ln(2)

To restore the bit deterministically to state 0, the barrier is lowered and an asymmetric bias applied, compressing the system into a single well:
Omega_final = 1 => S_final = k_B * ln(1) = 0

The change in entropy of the memory system is:
Delta S_system = S_final - S_initial = -k_B * ln(2)

By the Second Law of Thermodynamics, the total entropy of an isolated system cannot decrease:
Delta S_total = Delta S_system + Delta S_bath >= 0 => Delta S_bath >= k_B * ln(2)

The minimum heat dissipated into the environment is:
Q = T * Delta S_bath >= k_B * T * ln(2)
At T = 300 K, k_B * T * ln(2) approx 2.87 * 10^-21 Joules.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Thermodynamic cycle is strictly closed; no hidden un-reset memory states remain.",
                "Work extracted W = k_B * T * ln(2) exactly balances Landauer heat dissipation Q = k_B * T * ln(2).",
                "Second law preserved with strict equality for reversible erasure."
            ),
            tags = listOf("Physics", "Thermodynamics", "Information Theory", "Landauer", "Maxwells Demon")
        ),

        // ==========================================
        // 7. MATHEMATICAL PROOF: The Basel Problem
        // ==========================================
        ProblemChallenge(
            id = "math-basel-problem-euler",
            title = "The Basel Problem: Infinite Series sum(1/n^2)",
            domain = ProblemDomain.MATH,
            difficulty = ProblemDifficulty.INTERMEDIATE,
            subtitle = "Prove that the sum of the inverse squares of positive integers converges exactly to pi^2/6",
            description = """
The Basel problem, first posed by Pietro Mengoli in 1644, asked for the exact closed-form sum of the infinite series:
sum_{n=1}^{infinity} 1/n^2 = 1/1^2 + 1/2^2 + 1/3^2 + 1/4^2 + ...

Leading mathematicians including Jacob, Johann, and Daniel Bernoulli failed to find the value until Leonhard Euler solved it in 1734.

### Challenge:
1. Reconstruct Euler's brilliant insight connecting the Taylor series expansion of sin(x)/x with its infinite product expansion over its roots.
2. Extract the coefficient of x^2 in both representations to determine the exact value.
3. Address the modern rigor justification using the Weierstrass Factorization Theorem.
            """.trimIndent(),
            constraints = listOf(
                "Show exact trigonometric polynomial root matching.",
                "Demonstrate why roots occur at x = +/- pi, +/- 2pi, +/- 3pi, ...",
                "Conclude with exact numerical value approx 1.6449340668."
            ),
            starterPremiseOrCode = """
// Taylor Series:
// sin(x) = x - x^3/3! + x^5/5! - ...
// sin(x)/x = 1 - x^2/3! + x^4/5! - ...
// Roots of sin(x)/x = 0 are x = +/- n * pi
            """.trimIndent(),
            hints = listOf(
                "Hint 1: A polynomial with roots r_1, r_2, ... and constant term 1 can be factored as P(x) = product (1 - x/r_i).",
                "Hint 2: Pair the positive and negative roots: (1 - x/(n*pi))(1 + x/(n*pi)) = (1 - x^2/(n^2 * pi^2)).",
                "Hint 3: Multiply the product out and look at the coefficient of x^2. By Vieta's formulas, this coefficient is -sum_{n=1}^infinity 1/(n^2 * pi^2). Compare this with the Taylor series coefficient -1/3! = -1/6!"
            ),
            approach1Summary = "Numerical Approximation: Computing first 1,000 terms gives approx 1.6439, but cannot yield closed-form pi^2/6.",
            approach2Summary = "Euler Infinite Product Expansion: Equating the x^2 coefficients yields -1/pi^2 * sum(1/n^2) = -1/6 => sum(1/n^2) = pi^2/6.",
            optimalSolutionSnippet = """
### Euler's Derivation:
1. Taylor Series Expansion:
   sin(x)/x = 1 - x^2/3! + x^4/5! - ... = 1 - x^2/6 + x^4/120 - ...

2. Infinite Product over roots x = +/- pi, +/- 2pi, +/- 3pi, ...:
   sin(x)/x = product_{n=1}^{infinity} (1 - x^2 / (n^2 * pi^2))
   = (1 - x^2/pi^2)(1 - x^2/(4*pi^2))(1 - x^2/(9*pi^2))...

3. Expanding the product for the x^2 term:
   [x^2] = -(1/pi^2 + 1/(4*pi^2) + 1/(9*pi^2) + ...) = -1/pi^2 * sum_{n=1}^{infinity} (1/n^2)

4. Equating coefficients with the Taylor series:
   -1/pi^2 * sum_{n=1}^{infinity} (1/n^2) = -1/6
   sum_{n=1}^{infinity} (1/n^2) = pi^2/6
            """.trimIndent(),
            formalProofOrExplanation = """
### Rigorous Justification (Weierstrass Factorization Theorem):
Euler's method treated an entire function like an infinite-degree polynomial. In 19th-century complex analysis, Karl Weierstrass proved that any entire function with zeros a_n and finite genus h=1 has a canonical product:
sin(pi * z) = pi * z * product_{n=1}^{infinity} (1 - z^2/n^2)

Substituting x = pi * z, this confirms Euler's infinite product representation converges uniformly on compact subsets of C. Taking logarithmic derivatives:
pi * cot(pi * z) = 1/z - 2z * sum_{n=1}^{infinity} 1/(n^2 - z^2)

Expanding around z = 0 rigorously re-derives zeta(2) = pi^2/6 without any heuristic ambiguities.
            """.trimIndent(),
            verificationChecklist = listOf(
                "Taylor series coefficient -1/3! = -1/6 matches.",
                "Infinite product zeros at +/- n*pi match sinc roots.",
                "pi^2/6 approx 1.6449340668, verified against sum of first 10^6 terms."
            ),
            tags = listOf("Math", "Basel Problem", "Euler", "Riemann Zeta", "Infinite Series")
        )
    )

    fun getChallengeById(id: String): ProblemChallenge? {
        return challenges.find { it.id == id }
    }

    fun getChallengesByDomain(domain: ProblemDomain): List<ProblemChallenge> {
        return challenges.filter { it.domain == domain }
    }
}
