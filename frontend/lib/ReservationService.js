const API_ROUTE = `${process.env.NEXT_PUBLIC_API_URL}/reservations`;

export async function listMyReservations(token, setReservations, setError) {
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
        function_resources: resourceIds,
      }),
    });
    const data = await response.json();
    setNewReservation(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function cancelReservation(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/cancel`, {
      method: 'PATCH',
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
