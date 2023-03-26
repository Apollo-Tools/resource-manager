// Inspired by: https://flowbite.com/docs/components/badge/
import PropTypes from 'prop-types';

const ReservationStatusBadge = ({status, children}) => {
  let color;
  switch (status) {
    case 'NEW':
      color='blue';
      break;
    case 'DEPLOYED':
      color='green';
      break;
    case 'TERMINATING':
      color='orange';
      break;
    case 'ERROR':
      color='red';
      break;
    case 'TERMINATED':
    default:
      color='gray';
  }
  const className = `bg-${color}-100 text-${color}-500 text-xs font-medium mr-2 px-2.5 py-0.5 rounded border 
   border-solid`;
  return <span className={className}>{ children }</span>;
};

ReservationStatusBadge.propTypes = {
  status: PropTypes.oneOf(['NEW', 'DEPLOYED', 'TERMINATING', 'TERMINATED', 'ERROR']),
  children: PropTypes.node.isRequired,
};

export default ReservationStatusBadge;
