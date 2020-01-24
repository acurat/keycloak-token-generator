import React from 'react';
import './App.css';
import {Container, Tab} from 'semantic-ui-react';
import Keycloak from "./Keycloak";
import Custom from "./Custom";

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
            <h1> Token Generator </h1>
            <Tab panes={panes}/>
        </Container>
    );
};

export default App;
