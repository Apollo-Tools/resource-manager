const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/resources`

export async function listResources(token, setResources, setError) {
    try {
        const response = await fetch(`${API_ROUTE}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await response.json();
        setResources(() => data);
    } catch(error) {
        setError(true);
        console.log(error)
    }
}

export async function deleteResource(id, token, setError) {
    try {
        const response = await fetch(`${API_ROUTE}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.ok;
    } catch(error) {
        setError(true);
        console.log(error)
    }
}