import PropTypes from 'prop-types';

const DeploymentStatusCircle = ({isNew, isError, isTerminating, isDeployed, isTerminated}) => {
  let color = '';
  let animation = '';
  if (isError) {
    color = 'bg-red-400';
  } else if (isNew) {
    color = 'bg-orange-400';
    animation = 'animate-pulse';
  } else if (isDeployed) {
    color = 'bg-green-400';
  } else if (isTerminating) {
    color = 'bg-gray-400';
    animation = 'animate-pulse';
  } else if (isTerminated) {
    color = 'bg-gray-400';
  }
  const className = `inline-block w-5 h-5 rounded-full mr-1 ${color} ${animation}`;
  return <span className={className} />;
};

DeploymentStatusCircle.propTypes = {
  isNew: PropTypes.bool,
  isDeployed: PropTypes.bool,
  isTerminating: PropTypes.bool,
  isTerminated: PropTypes.bool,
  isError: PropTypes.bool,
};

export default DeploymentStatusCircle;
