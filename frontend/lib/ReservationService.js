import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/reservations`;

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

export async function listReservationLogs(reservationId, token, setReservationLogs, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${reservationId}/logs`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservationLogs(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function reserveResources(
    requestBody,
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
      body: JSON.stringify(requestBody),
    });
    const data = await response.json();
    setNewReservation(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

export async function getReservation(id, token, setReservation, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setReservation(() => data);
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
