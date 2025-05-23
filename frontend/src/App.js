import { useState } from 'react';

function App() {
    const [activeTab, setActiveTab] = useState('problem');
    const [topic, setTopic] = useState('recursion');
    const [difficulty, setDifficulty] = useState(1);
    const [problem, setProblem] = useState(null);
    const [loadingProblem, setLoadingProblem] = useState(false);
    const [studentId, setStudentId] = useState(0);
    const [gradeInput, setGradeInput] = useState('');
    const [gradeResult, setGradeResult] = useState(null);
    const [loadingGrade, setLoadingGrade] = useState(false);
    const [language, setLanguage] = useState('java');
    const [solveResult, setSolveResult] = useState(null);
    const [loadingSolve, setLoadingSolve] = useState(false);

    const handleGenerate = async () => {
        setLoadingProblem(true);
        setProblem(null);
        try {
            const res = await fetch(
                `http://localhost:8080/ai/problem?topic=${topic}&difficulty=${difficulty}`
            );
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
            const payload = {
                learningMaterial: problem,
                solution: gradeInput,
                studentId: studentId
            };
            const res = await fetch('http://localhost:8080/ai/submit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
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
        setLoadingSolve(true);
        setSolveResult(null);
        try {
            // POST the full LearningMaterial in the body,
            // and studentId + language as query params
            const res = await fetch(
                `http://localhost:8080/ai/solve?studentId=${studentId}&language=${language}`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(problem)
                }
            );
            if (!res.ok) throw new Error(`Status ${res.status}`);
            setSolveResult(await res.json());
        } catch (err) {
            setSolveResult({ error: err.message });
        } finally {
            setLoadingSolve(false);
        }
    };

    return (
        <div style={{ maxWidth: '700px', margin: '0 auto', padding: '2rem', fontFamily: 'system-ui' }}>
            <div style={{ marginBottom: '2rem' }}>
                <button onClick={() => setActiveTab('problem')} disabled={activeTab === 'problem'}>
                    Generate Problem
                </button>
                <button onClick={() => setActiveTab('grade')} disabled={activeTab === 'grade'}>
                    Grade Solution
                </button>
                <button onClick={() => setActiveTab('solve')} disabled={activeTab === 'solve'}>
                    Solver Aid
                </button>
            </div>

            {activeTab === 'problem' && (
                <>
                    <h1>TECMap Problem Generator</h1>
                    <label>
                        Topic:
                        <select
                            value={topic}
                            onChange={(e) => setTopic(e.target.value)}
                            style={{ marginLeft: '1rem' }}
                        >
                            <option value="arrays">Arrays</option>
                            <option value="recursion">Recursion</option>
                            <option value="linked lists">Linked Lists</option>
                            <option value="binary trees">Binary Trees</option>
                            <option value="hash maps">Hash Maps</option>
                        </select>
                    </label>
                    <br /><br />
                    <label>
                        Difficulty:
                        <input
                            type="number"
                            min="1"
                            max="5"
                            value={difficulty}
                            onChange={(e) => setDifficulty(Number(e.target.value))}
                            style={{ marginLeft: '1rem' }}
                        />
                    </label>
                    <br /><br />
                    <button onClick={handleGenerate} disabled={loadingProblem}>
                        {loadingProblem ? 'Generating...' : 'Generate Problem'}
                    </button>
                    <div
                        style={{
                            marginTop: '1rem',
                            padding: '1rem',
                            backgroundColor: '#3a2497',
                            border: '1px solid #ccc',
                            borderRadius: '6px',
                            whiteSpace: 'pre-wrap',
                            fontFamily: 'monospace',
                            maxWidth: '600px',
                            width: '100%',
                            color: '#fff'
                        }}
                    >
                        {problem?.content || 'No problem yet.'}
                    </div>
                </>
            )}

            {activeTab === 'grade' && (
                <>
                    <h1>TECMap Grader</h1>
                    <label>
                        Student ID:
                        <input
                            type="number"
                            value={studentId}
                            onChange={(e) => setStudentId(Number(e.target.value))}
                            style={{ marginLeft: '1rem', width: '5rem' }}
                        />
                    </label>
                    <br /><br />
                    <textarea
                        rows={10}
                        cols={60}
                        value={gradeInput}
                        onChange={(e) => setGradeInput(e.target.value)}
                        placeholder="Paste your solution here..."
                    />
                    <br />
                    <button onClick={handleGrade} disabled={loadingGrade}>
                        {loadingGrade ? 'Grading...' : 'Submit for Grade'}
                    </button>
                    <div style={{ marginTop: '1rem', whiteSpace: 'pre-wrap' }}>
                        <strong>Result:</strong>
                        <pre>{gradeResult ? JSON.stringify(gradeResult, null, 2) : 'No result yet.'}</pre>
                    </div>
                </>
            )}

            {activeTab === 'solve' && (
                <>
                    <h1>TECMap Solver Aider</h1>

                    <label>
                        Student ID:
                        <input
                            type="number"
                            value={studentId}
                            onChange={e => setStudentId(Number(e.target.value))}
                            style={{ marginLeft: '1rem', width: '5rem' }}
                        />
                    </label>
                    <br /><br />

                    <label>
                        Language:
                        <select
                            value={language}
                            onChange={e => setLanguage(e.target.value)}
                            style={{ marginLeft: '1rem' }}
                        >
                            <option value="java">Java</option>
                            <option value="python">Python</option>
                            <option value="cpp">C++</option>
                            {/* add any languages you support */}
                        </select>
                    </label>
                    <br /><br />

                    <button onClick={handleSolveAid} disabled={loadingSolve}>
                        {loadingSolve ? 'Asking model...' : 'Help Me Solve It'}
                    </button>

                    <div style={{ marginTop: '1rem', whiteSpace: 'pre-wrap' }}>
                        <strong>Hint / Partial Solution:</strong>
                        <pre>{solveResult || 'No hint yet.'}</pre>
                    </div>
                </>
            )}

        </div>
    );
}

export default App;
