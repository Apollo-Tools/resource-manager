const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/reservations`;

export async function listReservations(token, setReservations, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservations(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function reserveResources(
    resourceIds,
    token,
    setNewReservation,
    setError,
) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        resources: resourceIds,
        deploy_resources: true,
      }),
    });
    const data = await response.json();
    setNewReservation(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
