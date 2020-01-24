import React from 'react'
import {Modal} from 'semantic-ui-react'

interface Props {
    open: boolean;
    onClose: () => void;
}

const ModalComponent: React.FC<Props> = ({open, onClose, children}) => (
    <Modal size="small" open={open} onClose={onClose}>
        <Modal.Content>
            <>{children}</>
        </Modal.Content>
    </Modal>
);

export default ModalComponent