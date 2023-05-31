import {
  InfoCircleOutlined,
} from '@ant-design/icons';
import DateFormatter from '../misc/DateFormatter';
import {Button, Table, Space, Tooltip} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {listMyReservations} from '../../lib/ReservationService';
import Link from 'next/link';
import ReservationStatusBadge from './ReservationStatusBadge';

const {Column} = Table;

const ReservationTable = () => {
  const {token, checkTokenExpired} = useAuth();
  const [reservations, setReservations] = useState([]);
  const [statusFilter, setStatusFilter] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listMyReservations(token, setReservations, setError);
    }
  }, []);

  useEffect(() => {
    setStatusFilter(() =>
      [...new Set(reservations.map((reservation) => reservation.status_value))]
          .map((item) => {
            return {text: item, value: item};
          }));
  }, [reservations]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  return (
    <Table dataSource={ reservations } rowKey={ (record) => record.reservation_id } size="small">
      <Column title="Id" dataIndex="reservation_id" key="id"
        sorter={ (a, b) => a.reservation_id - b.reservation_id }
      />
      <Column title="Status" dataIndex="status_value" key="status_value"
        render={ (statusValue) => <ReservationStatusBadge status={statusValue}>{statusValue}</ReservationStatusBadge> }
        sorter={(a, b) =>
          a.status_value.localeCompare(b.status_value)}
        filters={statusFilter}
        onFilter={(value, record) => record.status_value.indexOf(value) === 0}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={ (createdAt) => <DateFormatter dateTimestamp={ createdAt }/> }
        sorter={ (a, b) => a.created_at - b.created_at }
      />
      <Column title="Action at" key="action"
        render={ (_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={ `/reservations/${ record.reservation_id }` }>
                <Button icon={ <InfoCircleOutlined/> }/>
              </Link>
            </Tooltip>
          </Space>
        ) }
      />
    </Table>
  );
};

export default ReservationTable;
