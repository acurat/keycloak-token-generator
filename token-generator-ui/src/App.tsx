import React from 'react';
import './App.css';
import {Container, Header, Tab} from 'semantic-ui-react';
import Keycloak from "./Keycloak";
import Custom from "./Custom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCoins} from '@fortawesome/free-solid-svg-icons';

const panes = [
    {
        menuItem: 'Custom Tokens',
        render: () => <Tab.Pane active><Custom/></Tab.Pane>,
    },
    {
        menuItem: 'Keycloak Tokens',
        render: () => <Tab.Pane><Keycloak/></Tab.Pane>,
    },

];


const App: React.FC = () => {
    return (
        <Container fluid className="App">
            <Header as='h1' textAlign={"center"}>Department of Tokens <FontAwesomeIcon icon={faCoins}/> </Header>
            <Tab panes={panes}/>
        </Container>
    );
};

export default App;
