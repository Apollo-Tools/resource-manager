const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/resource-types`;

export async function listResourceTypes(token, setResourceTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResourceTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}