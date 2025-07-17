export default function Product({ product }) {
  return (
    <>
      {/* Product */}
      <div className="bg-white rounded shadow overflow-hidden">
        {/* Product Image */}
        <img
          src={product.image}
          alt={product.name}
          className="w-full h-56 object-cover"
        />
        <div className="p-4">
          {/* Product Name */}
          <h2 className="font-semibold text-lg truncate">
            {product.name}
          </h2>
          {/* Product Description */}
          <p className="text-sm text-gray-500 truncate">
            {product.description}
          </p>
          <div className="mt-2 flex items-center justify-between">
            {/* Product Price */}
            <span className="font-bold">{product.price}</span>
            {/* Button of Adding To Cart */}
            <button className="bg-purple-600 text-white px-2 py-1 rounded hover:bg-purple-700">
              Add to cart
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
