class window.Paymill
        constructor: (@form) ->
            @paymentType = @form.find('.paymenttype.disabled').val() ? 'cc'
            @error = false

            @cardNumber = @form.find('.card-number')
            @cardCvc = @form.find('.card-cvc')
            @cardMonth = @form.find('.card-expiry-month')
            @cardYear = @form.find('.card-expiry-year')
            @cardHolder = @form.find('.card-holdername')
            @cardAmount = @form.find('.card-amount-int')
            @cardCurrency = @form.find('.card-currency')

            @elvNumber = @form.find('.elv-account')
            @elvBank = @form.find('.elv-bankcode')
            @elvHolder = @form.find('.elv-holdername')

        reload: ->
            @paymentType = @form.find('.paymenttype.disabled').val() ? 'cc'

        # Method to update amount and currency for 3DS credit card
        updatePrice: (amount, currency) ->
            @cardAmount.val parseFloat(amount) * 100
            @cardCurrency.val currency

        # General method to validate payment data
        validate: ->
            switch @paymentType
                when "cc" then @validateCc()
                when "elv" then @validateElv()
            return not @error

        # Method to validate credit card data
        validateCc: ->
            if not paymill.validateCardNumber @cardNumber.val()
                alert(translation["error"]["invalid-card-number"], @cardNumber)
                @error = true
            if not paymill.validateExpiry @cardMonth.val(), @cardYear.val()
                alert(translation["error"]["invalid-card-expiry-date"], @cardMonth)
                @error = true
            if not paymill.validateCvc @cardCvc.val(), @cardNumber.val()
                alert("Invalid verification code", @cardCvc)
                @error = true
            if not @cardHolder.val()?
                alert(translation["error"]["invalid-card-holdername"], @cardHolder)
                @error = true

        # Method to validate debit bank data
        validateElv: ->
            if not paymill.validateAccountNumber @elvAccount.val()
                alert(translation["error"]["invalid-elv-accountnumber"], @elvAccount)
                @error = true
            if not paymill.validateBankCode @elvBank.val()
                alert(translation["error"]["invalid-elv-bankcode"], @elvBank)
                @error = true
            if not @elvHolder.val()?
                alert(translation["error"]["invalid-elv-holdername"], @elvHolder)
                @error = true

        # Method to handle form submission
        submit: (responseHandler) ->
            return false if @error
            switch @paymentType
                when "ELV" then params = {
                        number: @elvNumber.val()
                        bank: @elvBank.val()
                        accountholder: @elvHolder.val()
                    }
                else params = {
                        number: @cardNumber.val()
                        exp_month: @cardMonth.val()
                        exp_year: @cardYear.val()
                        cvc: @cardCvc.val()
                        cardholder: @cardHolder.val()
                        amount: @cardAmount.val() * 100
                        currency: @cardCurrency.val()
                    }
            paymill.createToken(params, responseHandler)


$ ->
    paymill = new Paymill $("#billing-form")

    # Toggle payment form between credit card and direct debit
    $('.checkout .paymenttype').click ->
        $(this).addClass('btn-primary disabled');
        if $(this).val() is 'elv'
            $('#payment-form-elv').show()
            $('#payment-form-cc').hide()
            $('#billing-method:cc').removeClass('btn-primary disabled')
        else
            $('#payment-form-elv').hide()
            $('#payment-form-cc').show()
            $('#billing-method:elv').removeClass('btn-primary disabled')


    $("#billing-form button[type=submit]").click ->
        paymill.reload()
        # Validate form client side
        return false unless paymill.validate()
        # Submit payment data to Paymill
        paymill.submit (error, result) ->
            return alert(error.apierror) if error
            # Append token to checkout form
            $("#billing-form").find("input[name=paymillToken]").val result.token
            $("#billing-form").submit()
        return false