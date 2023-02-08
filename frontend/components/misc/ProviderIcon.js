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
