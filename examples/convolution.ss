func mmul(input: float[M*I*I], weights: float[N*M*K*K], output: float[N*O*O]) {

  for (let n = 0..N-1) {
    for (let m = 0..M-1) {
      for (let r = 0..O-1) {	
        for (let c = 0..O-1) {
	  for (let i = 0..K-1) unroll K {
	    for (let j = 0..K-1) unroll K {
	      let in_index  = m * I * I + ( r + i ) * I + ( c + j );
	      let w_index   = ( n * M + m ) * K * K + i * K + j;
	      let out_index = n * O * O + r * O + c; 
	      output[ out_index ] := input[ in_index ] * weights[ w_index ] + output[ out_index ];
	    }
	  }
	}
      }
    }
  }

}
