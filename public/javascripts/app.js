var stockApp = angular.module('stockApp', []).run(function($rootScope){
  console.log("App initialized");
});

stockApp.controller('TransactionController', function($scope,$http) {

  var defaultTransaction = {stock : "", op : "", opdate : "", quantity : ""};
  $scope.transaction = angular.copy(defaultTransaction);

  fetch();
  fetchMetaData();

  $scope.isNull = function(obj) {
      return typeof obj == 'undefined' || obj == null;
  }

  $scope.submit = function(transaction, transactionForm) {
    console.log(transaction);

    $http.post('/api', transaction)
         .success(function(data, status) {

            fetch();

            if (transactionForm) {
                transactionForm.$setPristine();
                transactionForm.$setUntouched();
            }

            $scope.transaction = angular.copy(defaultTransaction);

          })
         .error(function(data, status, headers, config) {
        	alert( "failure message: " + JSON.stringify({data: data}));
      	 });
  }

  function fetch() {

      $http.get("/api")
           .success(function(response) {
              $scope.transactionList = response
           })
           .error(function(data, status) {
             console.error('Transaction get error', status, data);
           });
  }


  function fetchMetaData() {

      $http.get("/api/meta")
           .success(function(response) {
               $scope.metaData = response
           })
           .error(function(data, status) {
               console.error('Meta data get error', status, data);
           });
    }

});