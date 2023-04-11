import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/metrics`;

export async function listMetrics(token, setMetrics, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
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
