const API_BASE = '/api';
let questions = [];
let currentQuestionIndex = 0;
let responses = {}; // map of index -> response object
let blueprint = null;
let timerInterval;
let timeLeft = 0;

function ensureName() {
    const userName = localStorage.getItem('userName');
    if (!userName) {
        window.location.href = 'index.html';
        return null;
    }
    const titleObj = document.getElementById('headerTitle') || document.getElementById('headerName');
    if (titleObj) titleObj.textContent = `${userName}'s BioQuest`;
    return userName;
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!ensureName()) return;

    const blueprintName = localStorage.getItem('currentBlueprint');
    if (!blueprintName) {
        alert("No blueprint selected!");
        window.location.href = 'index.html';
        return;
    }
    
    // Load blueprint to get duration
    try {
        const bpRes = await fetch(`${API_BASE}/blueprint/${blueprintName}`);
        blueprint = await bpRes.json();
        timeLeft = blueprint.duration * 60; 
        document.getElementById('examName').textContent = blueprint.name;
        startTimer();
    } catch(e) {
        console.error("Failed to load blueprint", e);
    }

    // Load exam questions
    try {
        const examRes = await fetch(`${API_BASE}/exam/${blueprintName}`);
        questions = await examRes.json();
        
        // Initialize responses
        questions.forEach((q, i) => {
            responses[i] = {
                questionId: q.id,
                userResponse: null,
                status: 'not_visited'
            };
        });
        
        buildPalette();
        showQuestion(0);
    } catch(e) {
        console.error("Failed to load questions", e);
    }
});

function startTimer() {
    timerInterval = setInterval(() => {
        timeLeft--;
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            submitExam();
        } else {
            const minutes = Math.floor(timeLeft / 60);
            const seconds = timeLeft % 60;
            document.getElementById('timeRemaining').textContent = 
                `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }
    }, 1000);
}

function buildPalette() {
    const container = document.getElementById('questionPalette');
    container.innerHTML = '';
    questions.forEach((q, i) => {
        const btn = document.createElement('button');
        btn.className = `pal-btn status-${responses[i].status}`;
        btn.textContent = i + 1;
        btn.onclick = () => showQuestion(i);
        btn.id = `pal-btn-${i}`;
        container.appendChild(btn);
    });
}

function updatePaletteBtn(index) {
    const btn = document.getElementById(`pal-btn-${index}`);
    btn.className = `pal-btn status-${responses[index].status}`;
}

function showQuestion(index) {
    // If navigating away from a 'not_visited' or 'not_answered' question without saving
    if (responses[currentQuestionIndex].status === 'not_visited') {
        responses[currentQuestionIndex].status = 'not_answered';
        updatePaletteBtn(currentQuestionIndex);
    }
    
    saveCurrentInputToResponse(); // auto-save what's clicked so far, status handled separately
    
    currentQuestionIndex = index;
    const q = questions[index];
    
    // Update header
    document.getElementById('qNumber').textContent = index + 1;
    document.getElementById('qType').textContent = `(${q.type})`;
    document.getElementById('qMarks').textContent = q.marks;
    
    // Update text
    document.getElementById('questionText').innerHTML = q.question;
    
    // Render input area
    const optContainer = document.getElementById('optionsContainer');
    const natContainer = document.getElementById('natContainer');
    const natInput = document.getElementById('natInput');
    
    optContainer.innerHTML = '';
    natContainer.style.display = 'none';
    natInput.value = '';

    if (q.type === 'MCQ' || q.type === 'MSQ') {
        optContainer.style.display = 'flex';
        const type = q.type === 'MCQ' ? 'radio' : 'checkbox';
        
        q.options.forEach((opt, i) => {
            const div = document.createElement('div');
            div.className = 'option-item';
            
            const input = document.createElement('input');
            input.type = type;
            input.name = `q-${index}`;
            input.value = i;
            input.id = `opt-${index}-${i}`;
            
            // Restore saved answer
            const savedAns = responses[index].userResponse;
            if (savedAns && savedAns.includes(i)) {
                input.checked = true;
            }
            
            const label = document.createElement('label');
            label.htmlFor = input.id;
            label.textContent = opt;
            
            div.appendChild(input);
            div.appendChild(label);
            optContainer.appendChild(div);
        });
    } else if (q.type === 'NAT') {
        natContainer.style.display = 'block';
        if (responses[index].userResponse != null) {
            natInput.value = responses[index].userResponse;
        }
    }

    if (responses[index].status === 'not_visited') {
        responses[index].status = 'not_answered';
        updatePaletteBtn(index);
    }
}

function saveCurrentInputToResponse() {
    const q = questions[currentQuestionIndex];
    if (q.type === 'MCQ' || q.type === 'MSQ') {
        const inputs = document.querySelectorAll(`input[name="q-${currentQuestionIndex}"]:checked`);
        const vals = Array.from(inputs).map(inp => parseInt(inp.value));
        responses[currentQuestionIndex].userResponse = vals.length > 0 ? vals : null;
    } else if (q.type === 'NAT') {
        const val = document.getElementById('natInput').value;
        responses[currentQuestionIndex].userResponse = val !== '' ? parseFloat(val) : null;
    }
}

function saveAndNext() {
    saveCurrentInputToResponse();
    const hasAns = responses[currentQuestionIndex].userResponse !== null;
    responses[currentQuestionIndex].status = hasAns ? 'answered' : 'not_answered';
    updatePaletteBtn(currentQuestionIndex);
    
    if (currentQuestionIndex < questions.length - 1) {
        showQuestion(currentQuestionIndex + 1);
    }
}

function markForReview() {
    saveCurrentInputToResponse();
    const hasAns = responses[currentQuestionIndex].userResponse !== null;
    responses[currentQuestionIndex].status = hasAns ? 'answered_review' : 'marked_review';
    updatePaletteBtn(currentQuestionIndex);
    
    if (currentQuestionIndex < questions.length - 1) {
        showQuestion(currentQuestionIndex + 1);
    }
}

function clearResponse() {
    const q = questions[currentQuestionIndex];
    if (q.type === 'MCQ' || q.type === 'MSQ') {
        document.querySelectorAll(`input[name="q-${currentQuestionIndex}"]`).forEach(inp => inp.checked = false);
    } else if (q.type === 'NAT') {
        document.getElementById('natInput').value = '';
    }
    responses[currentQuestionIndex].userResponse = null;
    responses[currentQuestionIndex].status = 'not_answered';
    updatePaletteBtn(currentQuestionIndex);
}

function toggleCalculator() {
    const modal = document.getElementById('calculatorModal');
    if (modal.style.display === "block") {
        modal.style.display = "none";
    } else {
        modal.style.display = "block";
    }
}

async function submitExam() {
    saveCurrentInputToResponse(); // save current
    clearInterval(timerInterval);
    
    const totalTimeAllowed = blueprint.duration * 60;
    const timeUsed = totalTimeAllowed - timeLeft;

    const submission = {
        attemptId: new Date().toISOString().replace(/[:.]/g, '-'), // safe filename
        timeUsed: timeUsed,
        responses: Object.values(responses)
    };
    
    try {
        const res = await fetch(`${API_BASE}/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(submission)
        });
        const result = await res.json();
        
        // Store result for review page
        localStorage.setItem('lastResult', JSON.stringify(result));
        window.location.href = 'review.html';
    } catch(e) {
        console.error("Submission failed", e);
        alert("Failed to submit exam!");
    }
}
