import { useState } from 'react';

function App() {
    const [activeTab, setActiveTab] = useState('problem');
    const [topic, setTopic] = useState('recursion');
    const [difficulty, setDifficulty] = useState(1);
    const [problem, setProblem] = useState(null);
    const [loading, setLoading] = useState(false);

    const [gradeInput, setGradeInput] = useState('');
    const [gradeResult, setGradeResult] = useState(null);

    const [solveInput, setSolveInput] = useState('');
    const [solveResult, setSolveResult] = useState(null);

    const handleGenerate = async () => {
        setLoading(true);
        setProblem(null);

        try {
            const res = await fetch(`http://localhost:8080/ai/problem?topic=${topic}&difficulty=${difficulty}`);
            const data = await res.json();
            setProblem(data);
        } catch (err) {
            setProblem({ error: err.message });
        }

        setLoading(false);
    };

    const handleGrade = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/ai/submit', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ solution: gradeInput })
            });
            const data = await res.json();
            setGradeResult(data);
        } catch (err) {
            setGradeResult({ error: err.message });
        }
        setLoading(false);
    };

    const handleSolveAid = async () => {
        setLoading(true);
        try {
            const res = await fetch('http://localhost:8080/ai/solve', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ problem: solveInput })
            });
            const data = await res.json();
            setSolveResult(data);
        } catch (err) {
            setSolveResult({ error: err.message });
        }
        setLoading(false);
    };

    return (
        <div style={{ padding: '2rem', fontFamily: 'system-ui', marginLeft: 'auto', marginRight: 'auto' }}>
            <div style={{ marginBottom: '2rem' }}>
                <button onClick={() => setActiveTab('problem')} disabled={activeTab === 'problem'}>Generate Problem</button>
                <button onClick={() => setActiveTab('grade')} disabled={activeTab === 'grade'}>Grade Solution</button>
                <button onClick={() => setActiveTab('solve')} disabled={activeTab === 'solve'}>Solver Aid</button>
            </div>

            {activeTab === 'problem' && (
                <>
            <h1>TECMap Problem Generator</h1>

            <label>
                Topic:
                <select value={topic} onChange={(e) => setTopic(e.target.value)} style={{ marginLeft: '1rem' }}>
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

            <button onClick={handleGenerate} disabled={loading}>
                {loading ? 'Generating...' : 'Generate Problem'}
            </button>

            <div style={{
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
            }}>
                {problem?.content || 'No problem yet.'}
            </div>
                </>
            )}

            {activeTab === 'grade' && (
                <>
                    <h1>TECMap Grader</h1>
                    <textarea
                        rows={10}
                        cols={60}
                        value={gradeInput}
                        onChange={(e) => setGradeInput(e.target.value)}
                        placeholder="Paste your solution here..."
                    />
                    <br />
                    <button onClick={handleGrade} disabled={loading}>
                        {loading ? 'Grading...' : 'Submit for Grade'}
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
                    <textarea
                        rows={10}
                        cols={60}
                        value={solveInput}
                        onChange={(e) => setSolveInput(e.target.value)}
                        placeholder="Paste your problem here..."
                    />
                    <br />
                    <button onClick={handleSolveAid} disabled={loading}>
                        {loading ? 'Asking model...' : 'Help Me Solve It'}
                    </button>
                    <div style={{ marginTop: '1rem', whiteSpace: 'pre-wrap' }}>
                        <strong>Hint:</strong>
                        <pre>{solveResult ? JSON.stringify(solveResult, null, 2) : 'No result yet.'}</pre>
                    </div>
                </>
            )}

        </div>
    );
}

export default App;
