import {Button, Divider, Modal, Typography} from 'antd';
import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {cancelReservation, getReservation, listReservationLogs} from '../../lib/ReservationService';
import ResourceReservationTable from '../../components/reservations/ResourceReservationTable';
import ReservationStatusCircle from '../../components/reservations/ReservationStatusCircle';
import {useInterval} from '../../lib/hooks/useInterval';
import LogsDisplay from '../../components/logs/LogsDisplay';
import {DisconnectOutlined, ExclamationCircleFilled} from '@ant-design/icons';

const {confirm} = Modal;

const ReservationDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [reservation, setReservation] = useState();
  const [logs, setLogs] = useState([]);
  const [pollingDelay, setPollingDelay] = useState();
  const [reservationStatus, setReservationStatus] = useState({
    isNew: false,
    isDeployed: false,
    isTerminating: false,
    isTerminated: false,
    isError: false,
  });
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id !== undefined) {
      refreshReservation();
    }
  }, [id]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('action failed');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (reservation != null) {
      checkReservationStatus();
    }
  }, [reservation]);

  useEffect(() => {
    if (reservation == null || reservationStatus.isNew || reservationStatus.isTerminating) {
      setPollingDelay(process.env.NEXT_PUBLIC_POLLING_DELAY);
    }
  }, [reservationStatus]);

  useInterval(async () => {
    if (!checkTokenExpired() && reservation != null) {
      await refreshReservation();
    }
  }, pollingDelay);

  const refreshReservation = async () => {
    setPollingDelay(null);
    await getReservation(id, token, setReservation, setError);
    await listReservationLogs(id, token, setLogs, setError);
  };

  const checkReservationStatus = () => {
    if (!Object.hasOwn(reservation, 'resource_reservations')) {
      return;
    }

    const resourceReservations = reservation.resource_reservations;
    setReservationStatus(() => {
      return {
        isNew: existResourceReservationsByStatusValue(resourceReservations, 'NEW'),
        isDeployed: existResourceReservationsByStatusValue(resourceReservations, 'DEPLOYED'),
        isTerminating: existResourceReservationsByStatusValue(resourceReservations, 'TERMINATING'),
        isTerminated: existResourceReservationsByStatusValue(resourceReservations, 'TERMINATED'),
        isError: existResourceReservationsByStatusValue(resourceReservations, 'ERROR'),
      };
    });
  };

  const existResourceReservationsByStatusValue = (resourceReservations, statusValue) => {
    return resourceReservations.filter((reservation) => {
      return reservation.status.status_value === statusValue;
    }).length !== 0;
  };

  const onClickCancel = async (id) => {
    if (!checkTokenExpired()) {
      await cancelReservation(id, token, setError)
          .then(() => refreshReservation());
    }
  };

  const showCancelConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled/>,
      content: 'Are you sure you want to cancel this reservation?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickCancel(id);
      },
    });
  };

  if (reservation == null) {
    return <></>;
  }

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={ 2 }>
        <ReservationStatusCircle isNew={reservationStatus.isNew}
          isDeployed={reservationStatus.isDeployed}
          isTerminating={reservationStatus.isTerminating}
          isTerminated={reservationStatus.isTerminated}
          isError={reservationStatus.isError}
        />
        Reservation Details ({ id })
        {reservationStatus.isDeployed &&
          <Button disabled={!reservation.is_active} onClick={ () => showCancelConfirm(id) }
            icon={ <DisconnectOutlined/> } className="ml-5"/>
        }
      </Typography.Title>
      <Divider/>
      {reservation.is_active &&
        <>
          <ResourceReservationTable resourceReservations={reservation.resource_reservations}/>
          <Divider />
        </>
      }
      <Typography.Title level={3}>Logs</Typography.Title>
      <LogsDisplay logs={logs}/>
      <Divider />
      {/* TODO: insert monitoring data*/}
      <Typography.Title level={3}>Monitoring</Typography.Title>
      <div>
        TODO display monitoring data
      </div>
    </div>
  );
};

export default ReservationDetails;
