const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/resource-types`;

export async function listResourceTypeMetrics(token, resourceTypeId, setMetrics, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceTypeId}/metrics`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setMetrics(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
