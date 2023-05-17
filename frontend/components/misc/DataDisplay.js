import {Typography} from 'antd';
import PropTypes from 'prop-types';

const {Title} = Typography;

const DataDisplay = ({label, children, className}) => {
  return (
    <div className={className}>
      <Title level={5} className="mt-0.5 mb-2">{label}</Title>
      {children}
    </div>
  );
};

DataDisplay.propTypes ={
  label: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

export default DataDisplay;
