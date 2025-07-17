import React,{useState} from 'react';
import {useNavigate,Link} from 'react-router-dom';
import axios from 'axios';

export default function SignupPage(){
    const [email,setEmail] = useState('');
    const [password,setPassword] = useState('');
    const [confirmPassword,setConfirmPassword]=useState('');
    }