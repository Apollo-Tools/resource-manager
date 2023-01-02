const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/resources`;

export async function listResourceMetrics(resourceId, token, setMetricValues, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setMetricValues(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function deleteResourceMetric(resourceId, metricId, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics/${metricId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}