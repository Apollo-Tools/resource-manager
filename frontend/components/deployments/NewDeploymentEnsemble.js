import EnsembleTable from '../ensembles/EnsembleTable';
import {Button} from 'antd';
import {useState} from 'react';
import PropTypes from 'prop-types';


const NewDeploymentEnsemble = ({value, next, setSelectedEnsemble}) => {
  const [selected, setSelected] = useState(value!=null);

  const rowSelection = {
    selectedRowKeys: [value],
    onChange: (selectedRowKeys) => {
      setSelected(true);
      setSelectedEnsemble(() => selectedRowKeys[0]);
    },
    type: 'radio',
  };

  return <>
    <EnsembleTable rowSelection={rowSelection}/>
    <Button type="primary" onClick={next} disabled={!selected} className="float-right">Next</Button>
  </>;
};

NewDeploymentEnsemble.propTypes = {
  value: PropTypes.number,
  next: PropTypes.func.isRequired,
  setSelectedEnsemble: PropTypes.func.isRequired,
};

export default NewDeploymentEnsemble;
