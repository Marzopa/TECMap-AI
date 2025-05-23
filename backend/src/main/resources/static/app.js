async function getProblem() {
    const studentId = document.getElementById("studentId").value;
    const password = document.getElementById("password").value;

    const res = await fetch(`/ai/problem?studentId=${studentId}&password=${password}`);
    if (!res.ok) return alert("Invalid credentials or error.");

    const data = await res.json();
    document.getElementById("problemBox").textContent = `Title: ${data.title}\nProblem: ${data.problem}`;
}

async function submitSolution() {
    const studentId = document.getElementById("studentId").value;
    const password = document.getElementById("password").value;
    const solution = document.getElementById("solution").value;

    const form = new URLSearchParams({ studentId, password, solution });

    const res = await fetch(`/ai/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form
    });

    if (!res.ok) return alert("Submission error.");

    const data = await res.json();
    document.getElementById("feedbackBox").textContent = `Grade: ${data.grade}\nFeedback: ${data.feedback}`;
}
