import {QuestionCircleOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {Tooltip} from 'antd';

const TooltipIcon = ({text}) => {
  return (
    <Tooltip placement="top" title={text} arrow={true} color={'#262626'}
      overlayClassName="w-fit max-w-sm text-center" overlayInnerStyle={{textAlign: 'center', padding: '6px'}}>
      <QuestionCircleOutlined className="mx-1"/>
    </Tooltip>
  );
};

TooltipIcon.propTypes = {
  text: PropTypes.node.isRequired,
};

export default TooltipIcon;
