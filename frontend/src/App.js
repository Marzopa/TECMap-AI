import { useState } from 'react';

function App() {
    const [topic, setTopic] = useState('recursion');
    const [difficulty, setDifficulty] = useState(1);
    const [problem, setProblem] = useState(null);
    const [loading, setLoading] = useState(false);

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

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
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

            <div style={{ marginTop: '2rem' }}>
                <h3>Generated Problem:</h3>
                <pre>{problem ? JSON.stringify(problem, null, 2) : 'No problem yet.'}</pre>
            </div>
        </div>
    );
}

export default App;
