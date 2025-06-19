import { useState, useEffect } from 'react';
import './App.css';

function useForm(initial) {
    const [values, setValues] = useState(initial);
    const update = field => e => {
        const v = e && e.target ? (e.target.type === 'checkbox' ? e.target.checked : e.target.type === 'number' ? Number(e.target.value) : e.target.value) : e;
        setValues(prev => ({ ...prev, [field]: v }));
    };
    return [values, update];
}

function App() {
    const [activeTab, setActiveTab] = useState('problem');
    const [problem, setProblem] = useState(null);
    const [loadingProblem, setLoadingProblem] = useState(false);
    const [studentId, setStudentId] = useState();
    const [gradeInput, setGradeInput] = useState('');
    const [gradeResult, setGradeResult] = useState(null);
    const [loadingGrade, setLoadingGrade] = useState(false);
    const [language, setLanguage] = useState('java');
    const [solveResult, setSolveResult] = useState('');
    const [loadingSolve, setLoadingSolve] = useState(false);
    const [approveUsername, setApproveUsername] = useState('');
    const [approvePassword, setApprovePassword] = useState('');
    const [approveProblemId, setApproveProblemId] = useState('');
    const [approveResult, setApproveResult] = useState(null);
    const [loadingApprove, setLoadingApprove] = useState(false);
    const [form, update] = useForm({
        topic: 'recursion',
        difficulty: 1,
        additionalTopicsInput: '',
        excludedTopicsInput: '',
        method: 'default',
        save: true
    });

    useEffect(() => {
        if (problem && problem.uuid) setApproveProblemId(problem.uuid);
    }, [problem]);

    const handleGenerate = async () => {
        setLoadingProblem(true);
        setProblem(null);
        try {
            const payload = {
                topic: form.topic,
                difficulty: form.difficulty,
                additionalTopics: form.additionalTopicsInput.split(',').map(s => s.trim()).filter(Boolean),
                excludedTopics: form.excludedTopicsInput.split(',').map(s => s.trim()).filter(Boolean),
                method: form.method,
                save: form.save,
            };
            const res = await fetch('http://localhost:8080/ai/problem', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(`Status ${res.status}`);
            setProblem(await res.json());
        } catch (err) {
            setProblem({ error: err.message });
        } finally {
            setLoadingProblem(false);
        }
    };

    const handleGrade = async () => {
        setLoadingGrade(true);
        setGradeResult(null);
        try {
            const res = await fetch('http://localhost:8080/ai/submit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    learningMaterial: problem,
                    solution: gradeInput,
                    studentId
                })
            });
            if (!res.ok) throw new Error(`Status ${res.status}`);
            setGradeResult(await res.json());
        } catch (err) {
            setGradeResult({ error: err.message });
        } finally {
            setLoadingGrade(false);
        }
    };

    const handleSolveAid = async () => {
        if (!problem) {
            setSolveResult('Generate a problem first.');
            return;
        }
        if (!studentId) {
            setSolveResult('Enter your Student ID above.');
            return;
        }
        setLoadingSolve(true);
        setSolveResult('');
        try {
            const res = await fetch('http://localhost:8080/ai/solve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ learningMaterial: problem, language, studentId })
            });
            if (!res.ok) throw new Error(`Status ${res.status}`);
            setSolveResult(await res.text());
        } catch (err) {
            setSolveResult(`Error: ${err.message}`);
        } finally {
            setLoadingSolve(false);
        }
    };

    const handleApprove = async () => {
        setLoadingApprove(true);
        setApproveResult(null);
        try {
            const res = await fetch('http://localhost:8080/ai/approve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: approveUsername,
                    password: approvePassword,
                    problemId: approveProblemId
                })
            });
            if (!res.ok) throw new Error(`Status ${res.status}`);
            setApproveResult(await res.text());
        } catch (err) {
            setApproveResult({ error: err.message });
        } finally {
            setLoadingApprove(false);
        }
    };

    return (
        <div className="container">
            <div className="tab-buttons">
                <button onClick={() => setActiveTab('problem')} disabled={activeTab === 'problem'}>
                    Generate Problem
                </button>
                <button onClick={() => setActiveTab('grade')} disabled={activeTab === 'grade'}>
                    Grade Solution
                </button>
                <button onClick={() => setActiveTab('solve')} disabled={activeTab === 'solve'}>
                    Solver Aid
                </button>
                <button onClick={() => setActiveTab('approve')} disabled={activeTab === 'approve'}>
                    Approve Problem
                </button>
            </div>

            {activeTab === 'problem' && (
                <>
                    <h1>TECMap Problem Generator</h1>
                    <label>
                        Topic:
                        <select value={form.topic} onChange={update('topic')}>
                            <option value="arrays">Arrays</option>
                            <option value="recursion">Recursion</option>
                            <option value="linked lists">Linked Lists</option>
                            <option value="binary trees">Binary Trees</option>
                            <option value="hash maps">Hash Maps</option>
                        </select>
                    </label>

                    <label>
                        Difficulty:
                        <input
                            type="number"
                            min="1"
                            max="5"
                            value={form.difficulty}
                            onChange={update('difficulty')}
                            className="input-small"
                        />
                    </label>

                    <label>
                        Additional Topics (comma-separated):
                        <input
                            type="text"
                            value={form.additionalTopicsInput}
                            onChange={update('additionalTopicsInput')}
                            className="input-wide"
                        />
                    </label>

                    <label>
                        Excluded Topics (comma-separated):
                        <input
                            type="text"
                            value={form.excludedTopicsInput}
                            onChange={update('excludedTopicsInput')}
                            className="input-wide"
                        />
                    </label>

                    <label>
                        Method:
                        <select value={form.method} onChange={update('method')}>
                            <option value="default">Default</option>
                            <option value="chase">Chase</option>
                            <option value="openai">OpenAI</option>
                        </select>
                    </label>

                    <label style={{ display: 'flex', alignItems: 'center', marginTop: '1rem' }}>
                        <input
                            type="checkbox"
                            checked={form.save}
                            onChange={update('save')}
                            style={{ marginRight: '0.5rem' }}
                        />
                        Save this problem to the database
                    </label>

                    <button onClick={handleGenerate} disabled={loadingProblem}>
                        {loadingProblem ? 'Generating…' : 'Generate Problem'}
                    </button>

                    <div className="output-box">
                        {problem?.content || 'No problem yet.'}
                    </div>
                </>
            )}

            {activeTab === 'grade' && (
                <>
                    <h1>TECMap Grader</h1>

                    <div className="sticky-problem">
                        {problem?.content || 'No problem yet.'}
                    </div>

                    <label>
                        Student ID:
                        <input
                            type="number"
                            min="100000000"
                            max="999999999"
                            required
                            value={studentId}
                            onChange={e => setStudentId(e.target.value)}
                            className="input-small no-spin"
                        />
                    </label>

                    <textarea
                        rows={10}
                        value={gradeInput}
                        onChange={e => setGradeInput(e.target.value)}
                        placeholder="Paste your solution here…"
                    />

                    <button onClick={handleGrade} disabled={loadingGrade}>
                        {loadingGrade ? 'Grading…' : 'Submit for Grade'}
                    </button>

                    <div className="output-box">
                        {gradeResult ? (
                            gradeResult.error ? (
                                <p>Error: {gradeResult.error}</p>
                            ) : (
                                <>
                                    <h3>Detected Language</h3>
                                    <p>{gradeResult.detectedLanguage}</p>

                                    <h3>Feedback</h3>
                                    <p>{gradeResult.feedback}</p>

                                    <h3>Grade</h3>
                                    <p>{gradeResult.grade}</p>
                                </>
                            )
                        ) : (
                            'No result yet.'
                        )}
                    </div>
                </>
            )}

            {activeTab === 'solve' && (
                <>
                    <h1>TECMap Solver Aider</h1>

                    <div className="sticky-problem">
                        {problem?.content || 'No problem yet.'}
                    </div>

                    <label>
                        Student ID:
                        <input
                            type="number"
                            min="100000000"
                            max="999999999"
                            required
                            value={studentId}
                            onChange={e => setStudentId(e.target.value)}
                            className="input-small no-spin"
                        />
                    </label>

                    <label>
                        Language:
                        <select value={language} onChange={e => setLanguage(e.target.value)}>
                            <option value="java">Java</option>
                            <option value="python">Python</option>
                            <option value="cpp">C++</option>
                        </select>
                    </label>

                    <button onClick={handleSolveAid} disabled={loadingSolve}>
                        {loadingSolve ? 'Asking model…' : 'Help Me Solve It'}
                    </button>

                    <div className="hint-box">
                        <strong>Hint / Partial Solution:</strong>
                        <div>{solveResult || 'No hint yet.'}</div>
                    </div>
                </>
            )}

            {activeTab === 'approve' && (
                <>
                    <h1>Approve Problem</h1>

                    <div className="sticky-problem">
                        {problem?.content || 'No problem yet.'}
                    </div>

                    <br/>

                    <b>Tags:</b> {problem?.tags?.length ? problem.tags.join(', ') : 'No tags available.'}

                    <label>
                        Username:
                        <input
                            type="text"
                            value={approveUsername}
                            onChange={e => setApproveUsername(e.target.value)}
                        />
                    </label>

                    <label>
                        Password:
                        <input
                            type="password"
                            value={approvePassword}
                            onChange={e => setApprovePassword(e.target.value)}
                        />
                    </label>

                    <button onClick={handleApprove} disabled={loadingApprove || !approveProblemId}>
                        {loadingApprove ? 'Approving…' : 'Approve'}
                    </button>

                    <div className="output-box">
                        {approveResult ?? 'No result yet.'}
                    </div>

                </>
            )}
        </div>
    );
}

export default App;
