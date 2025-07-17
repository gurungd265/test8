import React, { useState } from 'react';

const ProductPage = ({ product }) => {
  const [selectedImage, setSelectedImage] = useState(product.images[0].image_url);

  return (
    <div className="max-w-6xl mx-auto p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-semibold mb-4">{product.product_name}</h1>

      <div className="grid grid-cols-3 gap-8">
        {/* Left Section */}
        <div className="col-span-2">
          <div className="flex gap-4">
            {/* Mini Images */}
            <div className="flex flex-col gap-2">
              {product.images.map((img) => (
                <img
                  key={img.id}
                  src={img.image_url}
                  alt={product.product_name}
                  className={`w-16 h-16 object-cover cursor-pointer rounded border ${
                    selectedImage === img.image_url ? 'border-blue-500' : 'border-gray-300'
                  }`}
                  onClick={() => setSelectedImage(img.image_url)}
                />
              ))}
            </div>
            
            {/* Main Image */}
            <img
              src={selectedImage}
              alt={product.product_name}
              className="w-full h-[700px] object-cover rounded-lg border"
            />
          </div>

          {/* Rating and Reviews */}
          {product.rating && (
            <div className="mt-4">
              <span className="font-semibold">⭐ {product.rating}</span>
            </div>
          )}

          {product.reviews && (
            <div className="mt-4">
              <h3 className="font-semibold text-lg">Reviews:</h3>
              <ul className="list-disc ml-5">
                {product.reviews.map((review) => (
                  <li key={review.id}>{review.review}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Description */}
          <div className="mt-4">
            <h3 className="font-semibold text-lg">Description:</h3>
            <p>{product.description}</p>
          </div>

          {/* Content */}
          {product.content && (
            <div className="mt-4">
              <h3 className="font-semibold text-lg">Details:</h3>
              <p>{product.content}</p>
            </div>
          )}
        </div>

        {/* Right Section */}
        <div className="col-span-1 border rounded-lg p-4 flex flex-col gap-4">
          {/* Characteristics */}
          <div>
            <h3 className="font-semibold text-xl mb-2">Characteristics</h3>
            <ul className="space-y-2">
              {Object.entries(product.characteristics).map(([key, value]) => (
                <li key={key}>
                  <span className="font-medium capitalize">{key}: </span>{value}
                </li>
              ))}
            </ul>
          </div>

          {/* Price */}
          <div className="mt-auto">
            <div className="text-2xl font-semibold mb-4">{product.price} UZS</div>
            <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductPage;
