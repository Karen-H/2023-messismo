import React, { useState, useEffect } from 'react'
import { styled } from 'styled-components';
// import 'fontsource-roboto';
import { useSelector } from 'react-redux';
import Navbar from "../components/Navbar";
import { Navigate } from 'react-router-dom';
import user1 from '../images/users2/user-1.png';
import user2 from '../images/users2/user-2.png';
import user3 from '../images/users2/user-3.png';
import user4 from '../images/users2/user-4.png';
import user5 from '../images/users2/user-5.png';
import user6 from '../images/users2/user-6.png';
import user7 from '../images/users2/user-7.png';

const userimages = [user1, user2, user3, user4, user5, user6, user7];

const Container = styled.div`
    display: flex;
    flex-direction: column;
    height: 100vh;
`;

const MainContent = styled.div`
    display: ${props => (props.visible ? 'flex' : 'none')};
    flex-direction: column;
    justify-content: center;
    align-items: center;
    flex-grow: 1;
    
    @media(min-width: 601px) {
        flex-direction: row;
        gap: 8rem;
        justify-content: center;
        align-items: center;
    }
`;

const LeftSection = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    
    @media(min-width: 601px) {
        flex-shrink: 0;
    }
`;

const RightSection = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    
    @media(min-width: 601px) {
        flex-shrink: 0;
    }
`;

const WelcomeImage = styled.img`
    @media(max-width: 600px){
      width: 70%;
      margin-bottom: 2rem;
    }
    
    @media(min-width: 601px) {
        max-width: 400px;
        width: 100%;
    }
`;

const UserImage = styled.img`
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    margin-bottom: 0.5rem;
    border: 3px solid #a4d4cc;
    
    @media(max-width: 600px) {
        width: 100px;
        height: 100px;
        margin-bottom: 0.3rem;
    }
`;

const Resource = styled.div`
    font-family: 'Roboto',serif;
    color: #a4d4cc;
    text-transform: uppercase;
    letter-spacing: 1px;
    text-align: center;
    font-size: 20px;
    margin-top: 0.5rem;

    h3{
        font-size: 100%;
        margin: 0.5rem 0;
    }
    p{
        padding: 1rem;
    }
`;


function HomePage(){
    const [userImage, setUserImage] = useState(null);
    const { user: currentUser } = useSelector((state) => state.auth);
    const clicked = useSelector((state) => state.navigation.clicked);

    useEffect(() => {
        const randomIndex = Math.floor(Math.random() * userimages.length);
        setUserImage(userimages[randomIndex]);
    }, []);

    if (!currentUser) {
        return <Navigate to="/" />;
    }

    const contentVisible = !clicked;

    return(
        <Container>
            <Navbar />
            <MainContent visible={contentVisible}>
                <LeftSection>
                    <WelcomeImage src="/images/welcomeback2.png"/>
                </LeftSection>
                <RightSection>
                    {userImage && <UserImage src={userImage} alt="User" />}
                    <Resource>
                        <h3>{currentUser.username}</h3>
                        <h3>{currentUser.email}</h3>
                        <h3>{currentUser.role}</h3>
                    </Resource>
                </RightSection>
            </MainContent>
        </Container>
    )
}

export default HomePage;