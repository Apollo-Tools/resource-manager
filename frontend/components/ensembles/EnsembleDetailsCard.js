import PropTypes from 'prop-types';
import {Space} from 'antd';
import SloDisplay from '../misc/slos/SloDisplay';
import DateFormatter from '../misc/DateFormatter';
import TextDataDisplay from '../misc/TextDataDisplay';
import DataDisplay from '../misc/DataDisplay';
import ContentSkeleton from '../misc/ContentSkeleton';

const EnsembleDetailsCard = ({ensemble, isLoading, setError}) => {
  return (<Space direction="vertical" className="w-full" size='large'>
    <TextDataDisplay value={ensemble?.name} label="Name" isLoading={isLoading}/>
    {isLoading ?
      <ContentSkeleton titleProps={true} paragraphProps={false}/> :
      <DataDisplay label="Service Level Objectives">
        <SloDisplay slos={ensemble?.slos ?? []} setError={setError} />
      </DataDisplay>
    }
    <TextDataDisplay
      value={ensemble && <DateFormatter dateTimestamp={ensemble.created_at} includeTime/>}
      label="Created at"
      isLoading={isLoading}
    />
    <TextDataDisplay
      value={ensemble && <DateFormatter dateTimestamp={ensemble.updated_at} includeTime/>}
      label="Updated at"
      isLoading={isLoading}
    />
  </Space>
  );
};

EnsembleDetailsCard.propTypes = {
  ensemble: PropTypes.object,
  setError: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
};

export default EnsembleDetailsCard;
