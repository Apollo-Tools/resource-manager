const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/runtimes`;

export async function listRuntimes(token, setRuntimes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setRuntimes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
