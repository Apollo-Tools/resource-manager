const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/resource-providers`;

export async function listResourceProviders(token, setResourceProviders, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResourceProviders(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
