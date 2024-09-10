import {ItalicOutlined, AmazonOutlined, WindowsOutlined, GoogleOutlined, QuestionOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import Image from 'next/image';
import {Tooltip} from 'antd';


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
    case 'custom-edge':
      icon = <span className="relative top-1 inline-block">
        <Image src="/edge.svg" height="20" width="20" alt="Edge"/>
      </span>;
      break;
    case 'custom-fog':
      icon = <span className="relative top-1 inline-block">
        <Image src="/fog.svg" height="20" width="20" alt="Custom Fog"/>
      </span>;


      break;
    default:
      icon = <QuestionOutlined />;
      break;
  }

  return <span className={className}>
    <Tooltip placement="top" title={provider} arrow={true} color={'#262626'} overlayClassName="w-fit max-w-sm text-center" overlayInnerStyle={{textAlign: 'center', padding: '6px'}}>{icon}</Tooltip></span>;
};

ProviderIcon.propTypes = {
  provider: PropTypes.string,
  className: PropTypes.string,
};

export default ProviderIcon;
