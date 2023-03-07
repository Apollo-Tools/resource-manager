import {ItalicOutlined, AmazonOutlined, WindowsOutlined, GoogleOutlined, QuestionOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';


const ProviderIcon = ({provider, className}) => {
  let icon;
  switch (provider) {
    case 'aws':
      icon = <AmazonOutlined />;
      break;
    case 'ibm':
      icon = <ItalicOutlined />;
      break;
    case 'azure':
      icon = <WindowsOutlined />;
      break;
    case 'google':
      icon = <GoogleOutlined />;
      break;
    case 'edge':
      icon = <span className="relative top-1"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 120" width="1.4em" height="1.4em" fill="none"
        stroke="currentColor" strokeWidth="9" strokeLinecap="round" strokeLinejoin="round"
        className="text-black">
        <circle cx="60" cy="60" r="50"/>
        <text x="50%" y="55%" dominantBaseline="middle" textAnchor="middle" fontSize="70" fontWeight="100">E</text>
      </svg></span>;


      break;
    default:
      icon = <QuestionOutlined />;
      break;
  }

  return <span className={className}>{icon}</span>;
};

ProviderIcon.propTypes = {
  provider: PropTypes.string,
  className: PropTypes.string,
};

export default ProviderIcon;
