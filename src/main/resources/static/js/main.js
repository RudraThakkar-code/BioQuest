const API_BASE = '/api';

document.addEventListener('DOMContentLoaded', () => {
    const userName = localStorage.getItem('userName');
    
    if (userName) {
        document.getElementById('name-section').style.display = 'none';
        document.getElementById('welcome-section').style.display = 'block';
        document.getElementById('welcomeText').textContent = `Welcome back, ${userName}!`;
        document.getElementById('headerTitle').textContent = `${userName}'s BioQuest`;
    } else {
        document.getElementById('name-section').style.display = 'block';
        document.getElementById('welcome-section').style.display = 'none';
    }
});

function saveName() {
    const name = document.getElementById('userNameInput').value.trim();
    if (name) {
        localStorage.setItem('userName', name);
        location.reload();
    } else {
        alert('Please enter a valid name.');
    }
}

function changeName() {
    localStorage.removeItem('userName');
    location.reload();
}

function checkNameAndProceed(callback) {
    if (!localStorage.getItem('userName')) {
        alert("Please enter your name first.");
        return;
    }
    callback();
}

function startTestPrompt() {
    checkNameAndProceed(() => {
        document.getElementById('start-test-section').style.display = 'block';
    });
}

function startExam() {
    const blueprintName = document.getElementById('blueprintSelect').value;
    localStorage.setItem('currentBlueprint', blueprintName);
    window.location.href = 'exam.html';
}
