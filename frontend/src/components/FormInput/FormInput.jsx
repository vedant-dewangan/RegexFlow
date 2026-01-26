import { useState } from 'react';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import './FormInput.css';

function FormInput({
  label,
  type = 'text',
  id,
  name,
  placeholder,
  value,
  onChange,
  required = false,
  showPasswordToggle = false,
}) {
  const [showPassword, setShowPassword] = useState(false);
  const [isFocused, setIsFocused] = useState(false);

  const inputType = showPasswordToggle && showPassword ? 'text' : type;

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="form-input-group">
      <label htmlFor={id} className="form-input-label">
        {label}
        {required && <span className="required-asterisk">*</span>}
      </label>
      <div className="form-input-wrapper">
        <input
          type={inputType}
          id={id}
          name={name}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          required={required}
          className={`form-input ${isFocused ? 'focused' : ''} ${showPasswordToggle ? 'has-toggle' : ''}`}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
        />
        {showPasswordToggle && (
          <button
            type="button"
            className="password-toggle-btn"
            onClick={togglePasswordVisibility}
            aria-label={showPassword ? 'Hide password' : 'Show password'}
          >
            {showPassword ? <FaEyeSlash /> : <FaEye />}
          </button>
        )}
      </div>
    </div>
  );
}

export default FormInput;
