import PropTypes from 'prop-types';

const ReservationStatusCircle = ({isNew, isError, isTerminating, isDeployed, isTerminated}) => {
  let color = '';
  let animation = '';
  if (isError) {
    color = 'red';
  } else if (isNew) {
    color = 'orange';
    animation = 'animate-pulse';
  } else if (isDeployed) {
    color = 'green';
  } else if (isTerminating) {
    color = 'gray';
    animation = 'animate-pulse';
  } else if (isTerminated) {
    color = 'gray';
  }
  const className = `inline-block w-5 h-5 rounded-full mr-1 bg-${color}-400 ${animation}`;
  return <span className={className} />;
};

ReservationStatusCircle.propTypes = {
  isNew: PropTypes.bool,
  isDeployed: PropTypes.bool,
  isTerminating: PropTypes.bool,
  isTerminated: PropTypes.bool,
  isError: PropTypes.bool,
};

export default ReservationStatusCircle;
