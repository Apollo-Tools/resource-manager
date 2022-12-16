const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/accounts`

const fetcher = (...args) => fetch(...args).then(res => res.json())

export async function login(username, password, setToken, setError) {
    try {
        console.log(process.env.NEXT_PUBLIC_API_URL)
        const response = await fetch(`${API_ROUTE}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        const data = await response.json();
        setToken(data.token);
    } catch(error) {
        setError(true);
        console.log(error)
    }
}