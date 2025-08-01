import React, { useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCreditCard } from '@fortawesome/free-solid-svg-icons';

// このコンポーネントは、クレジットカード登録フォームのUIとロジックをカプセル化します。
// 親コンポーネントから onSubmit, isLoading, message などのプロパティを受け取ります。
export default function CreditCardRegistrationForm({ onSubmit, isLoading, message }) {
    const [cardNumber, setCardNumber] = useState('');
    const [expiryDate, setExpiryDate] = useState('');
    const [cvv, setCvv] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit({ cardNumber, expiryDate, cvv });
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2 className="text-2xl font-semibold mb-6 text-gray-700 flex items-center gap-2">
                <FontAwesomeIcon icon={faCreditCard} className="text-indigo-600" />
                クレジットカード登録
            </h2>
            {message && (
                <div className={`p-4 mb-4 rounded-md text-sm ${message.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {message.text}
                </div>
            )}
            <div className="mb-4">
                <label htmlFor="cardNumber" className="block text-sm font-medium text-gray-700 mb-2">カード番号</label>
                <input
                    type="text"
                    id="cardNumber"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="16桁のカード番号"
                    maxLength="16"
                    required
                />
            </div>
            <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                    <label htmlFor="expiryDate" className="block text-sm font-medium text-gray-700 mb-2">有効期限 (MM/YY)</label>
                    <input
                        type="text"
                        id="expiryDate"
                        value={expiryDate}
                        onChange={(e) => setExpiryDate(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        placeholder="MM/YY"
                        maxLength="5"
                        required
                    />
                </div>
                <div>
                    <label htmlFor="cvv" className="block text-sm font-medium text-gray-700 mb-2">CVV</label>
                    <input
                        type="text"
                        id="cvv"
                        value={cvv}
                        onChange={(e) => setCvv(e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        placeholder="3桁"
                        maxLength="3"
                        required
                    />
                </div>
            </div>
            <button
                type="submit"
                disabled={isLoading}
                className="w-full px-6 py-3 bg-indigo-600 text-white font-bold rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
                {isLoading ? '登録中...' : 'クレジットカードを登録する'}
            </button>
        </form>
    );
}