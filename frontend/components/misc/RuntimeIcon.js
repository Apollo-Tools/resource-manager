import {QuestionOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import Image from 'next/image';
import {Tooltip} from 'antd';


const ProviderIcon = ({runtime, className}) => {
  let icon;
  switch (runtime) {
    case 'python3.8':
      icon = <span className="relative top-1 inline-block">
        <Image src="/python.svg" height="20" width="20" alt="PY"/>
      </span>;
      break;
    case 'java11':
      icon = <span className="relative top-1 inline-block">
        <Image src="/java.svg" height="20" width="20" alt="JDK"/>
      </span>;
      break;
    default:
      icon = <QuestionOutlined />;
      break;
  }

  return <span className={className}>
    <Tooltip placement="top" title={runtime} arrow={true} color={'#262626'}
      overlayClassName="w-fit max-w-sm text-center"
      overlayInnerStyle={{textAlign: 'center', padding: '6px'}}
    >
      {icon}
    </Tooltip>
  </span>;
};

ProviderIcon.propTypes = {
  runtime: PropTypes.string,
  className: PropTypes.string,
};

export default ProviderIcon;
