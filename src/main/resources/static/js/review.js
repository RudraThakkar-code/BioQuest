function ensureName() {
    const userName = localStorage.getItem('userName');
    if (!userName) {
        window.location.href = 'index.html';
        return null;
    }
    const titleObj = document.getElementById('headerTitle');
    if (titleObj) titleObj.textContent = `${userName}'s BioQuest - Review Mode`;
    return userName;
}

document.addEventListener('DOMContentLoaded', () => {
    if (!ensureName()) return;

    const resultJson = localStorage.getItem('lastResult');
    if (!resultJson) {
        alert("No recent test result found.");
        window.location.href = 'index.html';
        return;
    }
    
    const result = JSON.parse(resultJson);
    
    document.getElementById('totalMarks').textContent = result.totalMarks.toFixed(2);
    document.getElementById('totalAttempted').textContent = result.totalAttempted;
    document.getElementById('totalCorrect').textContent = result.totalCorrect;
    
    const container = document.getElementById('reviewContainer');
    
    result.questionResults.forEach((qr, i) => {
        const q = qr.question;
        const div = document.createElement('div');
        div.className = `card review-item ${qr.marksAwarded > 0 ? 'correct' : (qr.userResponse ? 'wrong' : 'unattempted')}`;
        
        let userAnsStr = 'Not Attempted';
        if (qr.userResponse) {
            if (Array.isArray(qr.userResponse)) {
                userAnsStr = qr.userResponse.map(idx => q.options[idx]).join(', ');
            } else {
                userAnsStr = qr.userResponse;
            }
        }
        
        let correctAnsStr = '';
        if (q.type === 'MCQ' || q.type === 'MSQ') {
            correctAnsStr = q.answer.map(idx => q.options[idx]).join(', ');
        } else {
            correctAnsStr = q.answer + (q.tolerance > 0 ? ` (±${q.tolerance})` : '');
        }

        div.innerHTML = `
            <div class="review-header">
                <span>Q${i+1} (${q.type}) - ${q.subject}: ${q.topic}</span>
                <span>Marks Awarded: ${qr.marksAwarded.toFixed(2)} / ${q.marks}</span>
            </div>
            <div class="review-question">${q.question}</div>
            
            ${q.options ? `<div style="margin-bottom:10px; font-size:0.9rem;">
                <strong>Options:</strong>
                <ul style="list-style-type:none; padding-left:10px;">
                    ${q.options.map((opt, idx) => `<li>${idx}: ${opt}</li>`).join('')}
                </ul>
            </div>` : ''}

            <div class="review-ans"><strong>Your Answer:</strong> ${userAnsStr}</div>
            <div class="review-ans"><strong>Correct Answer:</strong> ${correctAnsStr}</div>
            
            <div class="explanation">
                <strong>Explanation:</strong> ${q.explanation} <br>
                <em>Source: ${q.source}</em>
            </div>
        `;
        container.appendChild(div);
    });
});
