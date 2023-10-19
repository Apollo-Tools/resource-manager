import {Typography} from 'antd';
import PropTypes from 'prop-types';
import TooltipIcon from './TooltipIcon';

const {Title} = Typography;

const DataDisplay = ({label, tooltip, children, className}) => {
  return (
    <div className={className}>
      <Title level={5} className="mt-0.5 mb-2">
        {label}{(tooltip && <TooltipIcon text={tooltip} />)}
      </Title>
      {children}
    </div>
  );
};

DataDisplay.propTypes ={
  label: PropTypes.string.isRequired,
  tooltip: PropTypes.string,
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

export default DataDisplay;
