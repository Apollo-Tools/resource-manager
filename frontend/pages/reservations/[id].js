import { Divider, Typography } from 'antd';
import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import { useAuth } from '../../lib/AuthenticationProvider';
import { getReservation } from '../../lib/ReservationService';
import ResourceReservationTable from '../../components/reservations/ResourceReservationTable';
import { useInterval } from '../../lib/hooks/useInterval';

const ReservationDetails = () => {
  const { token, checkTokenExpired } = useAuth();
  const [error, setError] = useState(false);
  const [reservation, setReservation] = useState();
  const [pollingDelay, setPollingDelay] = useState();
  const router = useRouter();
  const { id } = router.query;

  useEffect(() => {
    if (!checkTokenExpired()) {
      refreshReservation()
    }
  }, [id]);

  useInterval(() => {
    refreshReservation();
  }, pollingDelay)

  const refreshReservation = async () => {
    setPollingDelay(null);
    await getReservation(id, token, setReservation, setError);
    if (reservation == null || (reservation.is_active && !checkAllDeployed())) {
      setPollingDelay(process.env.NEXT_PUBLIC_POLLING_DELAY)
    }
  }

  const checkAllDeployed = () => {
    return reservation.resource_reservations.filter((status) => {
      return !status.is_deployed
    }).length === 0
  }
  if (reservation == null) {
    return <></>
  }

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={ 2 }>
        <span className={"inline-block w-5 h-5 rounded-full mr-1 " +
          (!reservation.is_active ? "bg-red-400" : (checkAllDeployed() ? "bg-green-400" : "bg-orange-400"))} />
        Reservation Details ({ id })
      </Typography.Title>
      <Divider/>
      {reservation.is_active &&
        <>
          <ResourceReservationTable  resourceReservations={reservation.resource_reservations}/>
          <Divider />
          {/*TODO: insert monitoring data*/}
          <div>
            TODO display monitoring data
          </div>
        </>
      }
    </div>
  );
};

export default ReservationDetails;