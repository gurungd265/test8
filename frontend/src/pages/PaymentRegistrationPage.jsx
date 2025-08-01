import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCreditCard, faMobileAlt } from '@fortawesome/free-solid-svg-icons';

// コンポーネントのインポートパスを修正しました
import PaypayRegistrationForm from '../components/payment/PaypayRegistrationForm';
import CreditCardRegistrationForm from '../components/payment/CreditCardRegistrationForm';

// バックエンドAPIがまだないので、ダミーAPI関数を使用します。
const mockApi = {
    registerPaypay: (userId, paypayId) => {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if (paypayId) {
                    console.log(`PayPayアカウント登録成功: User=${userId}, ID=${paypayId}`);
                    resolve({ message: 'PayPayアカウントの登録が完了しました。' });
                } else {
                    reject({ message: 'PayPay IDは必須項目です。' });
                }
            }, 1000);
        });
    },
    registerCreditCard: (userId, cardInfo) => {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const { cardNumber, expiryDate, cvv } = cardInfo;
                if (!cardNumber || cardNumber.length !== 16) {
                    return reject({ message: '有効な16桁のカード番号を入力してください。' });
                }
                if (!expiryDate || !/^(0[1-9]|1[0-2])\/\d{2}$/.test(expiryDate)) {
                    return reject({ message: '有効な有効期限(MM/YY)を入力してください。' });
                }
                if (!cvv || cvv.length !== 3) {
                    return reject({ message: '有効な3桁のCVVを入力してください。' });
                }
                console.log(`クレジットカード登録成功: User=${userId}, Card=${cardNumber}`);
                resolve({ message: 'クレジットカードの登録が完了しました。' });
            }, 1500);
        });
    }
};

export default function PaymentRegistrationPage() {
    const { user, isLoggedIn } = useAuth();
    const navigate = useNavigate();
    const [selectedMethod, setSelectedMethod] = useState('paypay');
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState(null);

    const handlePaypaySubmit = async ({ paypayId }) => {
        setIsLoading(true);
        setMessage(null);
        try {
            const res = await mockApi.registerPaypay(user.email, paypayId);
            setMessage({ type: 'success', text: res.message });
        } catch (err) {
            setMessage({ type: 'error', text: err.message });
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreditCardSubmit = async (cardInfo) => {
        setIsLoading(true);
        setMessage(null);
        try {
            const res = await mockApi.registerCreditCard(user.email, cardInfo);
            setMessage({ type: 'success', text: res.message });
        } catch (err) {
            setMessage({ type: 'error', text: err.message });
        } finally {
            setIsLoading(false);
        }
    };

    if (!isLoggedIn) {
        return (
            <div className="container mx-auto px-4 py-8 max-w-4xl text-center text-gray-700">
                <p>このページにアクセスするにはログインが必要です。</p>
                <button onClick={() => navigate('/login')} className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                    ログインページへ
                </button>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
            <h1 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">決済手段登録</h1>

            <div className="flex justify-center mb-8 gap-4">
                <button
                    onClick={() => setSelectedMethod('paypay')}
                    className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                        selectedMethod === 'paypay' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                >
                    <FontAwesomeIcon icon={faMobileAlt} />
                    <span>PayPay登録</span>
                </button>
                <button
                    onClick={() => setSelectedMethod('creditCard')}
                    className={`px-6 py-3 rounded-md font-semibold transition-colors flex items-center gap-2 ${
                        selectedMethod === 'creditCard' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                >
                    <FontAwesomeIcon icon={faCreditCard} />
                    <span>クレジットカード登録</span>
                </button>
            </div>

            <div className="bg-white p-8 rounded-lg shadow-lg border border-gray-200">
                {selectedMethod === 'paypay' ? (
                    <PaypayRegistrationForm
                        onSubmit={handlePaypaySubmit}
                        isLoading={isLoading}
                        message={message}
                    />
                ) : (
                    <CreditCardRegistrationForm
                        onSubmit={handleCreditCardSubmit}
                        isLoading={isLoading}
                        message={message}
                    />
                )}
            </div>

            <div className="mt-8 text-center">
                <button
                    onClick={() => navigate('/')}
                    className="px-6 py-3 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-colors"
                >
                    メインページに戻る
                </button>
            </div>
        </div>
    );
}