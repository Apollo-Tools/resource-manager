const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/regions`;

export async function listRegions(token, setRegions, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setRegions(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
