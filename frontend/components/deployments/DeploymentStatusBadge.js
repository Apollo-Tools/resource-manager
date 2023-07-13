// Inspired by: https://flowbite.com/docs/components/badge/
import PropTypes from 'prop-types';

const DeploymentStatusBadge = ({status, children}) => {
  let bgColor;
  let textColor;
  // bgColor and textColor have to be set this way because tailwind generates the necessary css classes only if it can
  // detect them in the code (https://stackoverflow.com/questions/71063619/react-and-tailwind-css-dynamically-generated-classes-are-not-being-applied)
  switch (status) {
    case 'NEW':
      bgColor='bg-blue-100';
      textColor = 'text-blue-500';
      break;
    case 'DEPLOYED':
      bgColor='bg-green-100';
      textColor = 'text-green-500';
      break;
    case 'TERMINATING':
      bgColor='bg-orange-100';
      textColor = 'text-orange-500';
      break;
    case 'ERROR':
      bgColor='bg-red-100';
      textColor = 'text-red-500';
      break;
    case 'bg-TERMINATED-100':
    default:
      bgColor='bg-gray-100';
      textColor = 'text-gray-500';
  }
  const className = `${bgColor} ${textColor} text-xs font-medium mr-2 px-2.5 py-0.5 rounded border 
   border-solid`;

  return <span className={className}>{ children }</span>;
};

DeploymentStatusBadge.propTypes = {
  status: PropTypes.oneOf(['NEW', 'DEPLOYED', 'TERMINATING', 'TERMINATED', 'ERROR']),
  children: PropTypes.node.isRequired,
};

export default DeploymentStatusBadge;
